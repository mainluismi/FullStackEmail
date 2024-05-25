package com.example.EjercicioEmailLogin.EmailLoginLuismi.Controller;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Email.EmailService;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Service.UsuarioService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    static String auxCode;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    @PostMapping("/enviarCodigo")
    public ResponseEntity<?> enviarCodigo(@RequestBody Usuario usuario) {
        try {
            // Ver si el email está presente en la solicitud
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"El parámetro 'email' no está presente en la solicitud.\"}");
            }

            // Verificar si el email ya existe en la base de datos
            Usuario usuarioExistente = usuarioService.findByEmail(usuario.getEmail());
            if (usuarioExistente != null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Error: el email ya existe\"}");
            }

            // Generar y enviar el código de verificación
            String codigoVerificacion = emailService.enviarCodigoVerificacion(usuario.getEmail());
            auxCode = codigoVerificacion;
            System.err.println("Se ha enviado el correo perfectamente: " + codigoVerificacion);
            usuario.setCodigoVerificacion(codigoVerificacion);

            // Crear un objeto JSON que incluya el email
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", "Código de verificación enviado correctamente.");
            jsonResponse.put("email", usuario.getEmail());
            jsonResponse.put("codigoVerificacion", codigoVerificacion);

            // Devolver una respuesta JSON con el mensaje de éxito y el email
            return ResponseEntity.ok().body(jsonResponse.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Error al procesar la solicitud. Por favor, inténtelo de nuevo.\"}");
        }
    }

    //falta arreglar que siempre es correcto el codigo de verificacion
    @PostMapping("/verificarCodigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Usuario usuario) {
        try {
            String email = usuario.getEmail();
            String codigo = usuario.getCodigoVerificacion();
            String nombre = usuario.getNombre();
            String apellidos = usuario.getApellidos();
            String password = usuario.getPassword();

            //Depuracion para ver datos almacenados
            System.out.println("El email es: "+email);
            System.out.println("El codigo es: "+codigo);
            System.out.println("El nombre es: "+nombre);
            System.out.println("El apellidos es: "+apellidos);
            System.out.println("La contraseña es: "+password);

            // Obtener el usuario por su email
            if (email != null) {
                String codigoAlmacenado = auxCode;
                if (codigoAlmacenado.equals(codigo)) {
                    // Código de verificación correcto, actualiza el estado de verificación del usuario
                    usuario.setVerificado(true);
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(email);
                    nuevoUsuario.setCodigoVerificacion(codigo);
                    nuevoUsuario.setNombre(nombre);
                    nuevoUsuario.setApellidos(apellidos);
                    nuevoUsuario.setPassword(password);
                    nuevoUsuario.setVerificado(true); // Opcional: puedes definir si el usuario se verificó al registrarse
                    usuarioService.guardarUsuario(nuevoUsuario);
                    return ResponseEntity.ok().body("{\"message\": \"Código de verificación correcto.\"}");
                } else {
                    return ResponseEntity.badRequest().body("{\"error\": \"Código de verificación incorrecto.\"}");
                }
            } else {
                // Si el usuario no existe, crear uno nuevo con los datos proporcionados

                return ResponseEntity.ok().body("{\"message\": \"Usuario creado y verificado correctamente.\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error al procesar la solicitud. Por favor, inténtelo de nuevo.\"}");
        }
    }




    @PostMapping("/verificarUsuario")
    public ResponseEntity<?> verificarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario usuarioExistente = usuarioService.findByEmail(usuario.getEmail());
            if (usuarioExistente != null) {
                // El usuario ya está registrado
                return ResponseEntity.ok().body("El usuario ya está registrado");
            } else {
                // El usuario no está registrado
                return ResponseEntity.ok().body("El usuario no está registrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
        }
    }


    @PostMapping("/guardarUsuario")
    public ResponseEntity<?> guardarUsuario(@RequestBody Usuario usuario) {
        try {
            String codigoAlmacenado = usuario.getCodigoVerificacion();
            if (codigoAlmacenado.equals(usuario.getCodigoVerificacion())) {
                usuario.setVerificado(true);
                usuarioService.guardarUsuario(usuario);
                return ResponseEntity.ok().body("{\"message\": \"Usuario registrado correctamente\"}");
            } else {
                return ResponseEntity.badRequest().body("{\"error\": \"Código de verificación incorrecto\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Error al procesar la solicitud. Por favor, inténtelo de nuevo.\"}");
        }
    }


    @PostMapping("/inicioSesion")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario usuario) {
        // Verificar si las credenciales corresponden al administrador
        if (usuarioService.verificarAdmin(usuario.getEmail(), usuario.getPassword())) {
            // Si las credenciales son del administrador, devuelve una respuesta exitosa
            return ResponseEntity.ok().body("{\"isAdmin\": true}");
        } else {
            // Si las credenciales no son del administrador, intenta autenticar al usuario normalmente
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(usuario.getEmail(), usuario.getPassword());
            if (usuarioAutenticado != null) {
                return ResponseEntity.ok().body(usuarioAutenticado);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"isAdmin\": false}");
            }
        }
    }



    @GetMapping("/admin")
    public ResponseEntity<?> eliminarUsuarioByAdmin(@RequestParam("email") String emailUsuario) {
        usuarioService.eliminarUsuarioPorEmail(emailUsuario);
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok().body(listaUsuarios);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodosLosUsuarios() {
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        if (listaUsuarios != null && !listaUsuarios.isEmpty()) {
            return ResponseEntity.ok().body(listaUsuarios);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //Metodo auxiliar formularios
    @PostMapping("/api/generate-form")
    public String generateForm(@RequestBody List<String> questions) {
        // Lógica para generar el formulario HTML basado en las preguntas
        StringBuilder htmlForm = new StringBuilder("<form>");
        for (String question : questions) {
            htmlForm.append("<label>").append(question).append("</label>");
            htmlForm.append("<input type=\"text\" required /><br>");
        }
        htmlForm.append("</form>");
        System.out.println(htmlForm.toString());
        return htmlForm.toString();
    }

}

