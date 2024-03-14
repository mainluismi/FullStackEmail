package com.example.EjercicioEmailLogin.EmailLoginLuismi.Controller;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Email.EmailService;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Service.UsuarioService;
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

    @Autowired
    public UsuarioController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    @PostMapping("/confirmarCorreo")
    public ResponseEntity<?> confirmarCorreo(@RequestBody Usuario usuario) {
        try {
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("El parámetro 'email' no está presente en la solicitud.");
            }


            // Verificar si el correo electrónico ya existe en la base de datos
            Usuario usuarioExistente = usuarioService.findByEmail(usuario.getEmail());
            if (usuarioExistente != null) {
                return ResponseEntity.badRequest().body("Error: el email ya existe");
            }

            String codigoVerificacion = emailService.enviarCodigoVerificacion(usuario.getEmail());
            usuario.setCodigoVerificacion(codigoVerificacion);

            return ResponseEntity.ok().body(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
        }
    }


    @PostMapping("/verificarCodigo")
    public ResponseEntity<?> verificarCodigo(@RequestParam("email") String email, @RequestParam("codigo") String codigo) {
        try {
            Usuario usuarioExistente = usuarioService.findByEmail(email);
            if (usuarioExistente == null) {
                return ResponseEntity.badRequest().body("Error: el usuario no existe");
            }

            String codigoAlmacenado = usuarioExistente.getCodigoVerificacion();
            if (codigoAlmacenado.equals(codigo)) {
                return ResponseEntity.ok().body("Código de verificación correcto");
            } else {
                return ResponseEntity.badRequest().body("Código de verificación incorrecto");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
        }
    }


    @PostMapping("/guardarUsuario")
    public ResponseEntity<?> guardarUsuario(@RequestBody Usuario usuario) {
        try {
            String codigoAlmacenado = usuario.getCodigoVerificacion();
            if (codigoAlmacenado.equals(usuario.getCodigoVerificacion())) {
                usuario.setVerificado(true);
                usuarioService.guardarUsuario(usuario);
                return ResponseEntity.ok().body("Usuario registrado correctamente");
            } else {
                return ResponseEntity.badRequest().body("Código de verificación incorrecto");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
        }
    }

    @PostMapping("/inicioSesion")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario usuario) {
        Usuario usuarioAutenticado = usuarioService.autenticarUsuario(usuario.getEmail(), usuario.getPassword());
        if (usuarioAutenticado != null) {
            return ResponseEntity.ok().body(usuarioAutenticado);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo o contraseña incorrectos");
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

}

