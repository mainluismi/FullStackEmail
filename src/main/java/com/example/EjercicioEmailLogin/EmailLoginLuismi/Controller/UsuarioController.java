package com.example.EjercicioEmailLogin.EmailLoginLuismi.Controller;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Email.EmailService;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private String codigoAlmacenado;


    @Autowired
    public UsuarioController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }
    @GetMapping("/home")
    public String logUsuario(){
        return "home";
    }

    @GetMapping("/formularioRegistro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "formularioRegistro";
    }

    /*
    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam(name = "email", required = false, defaultValue = "") String email,
                                 @RequestParam(name = "nombre", required = false, defaultValue = "") String nombre,
                                 @RequestParam(name = "apellidos", required = false, defaultValue = "") String apellidos,
                                 @RequestParam(name = "password", required = false, defaultValue = "") String password, Model model) {
        email = usuario.getEmail();
        nombre = usuario.getNombre();
        apellidos = usuario.getApellidos();
        password = usuario.getPassword();
        if (email.isEmpty()) {
            // El parámetro email no está presente en la solicitud
            // Puedes manejar este caso según tus necesidades, por ejemplo, mostrando un mensaje de error
            System.err.println("El parámetro 'email' no está presente en la solicitud.");
            return "redirect:/error";  // Puedes redirigir a una página de error o hacer cualquier otra cosa
        }

        // Generar y enviar el código de verificación
        String codigoVerificacion = emailService.enviarCodigoVerificacion(email);
        System.err.println(codigoVerificacion);
        System.err.println(email);
        System.err.println(nombre);
        System.err.println(apellidos);
        System.err.println(password);

        // Almacenar el código de verificación junto con el usuario (pero no guardarlo en la base de datos)
        usuario.setCodigoVerificacion(codigoVerificacion);
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setPassword(password);

        // Almacena el usuario en el modelo para que se pueda utilizar en la vista de confirmación
        model.addAttribute("usuario", usuario);
        model.addAttribute("email", email);
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellidos", apellidos);
        model.addAttribute("password", password);

        // Devolver el nombre de la vista en lugar de realizar una redirección
        return "confirmacionCorreo";
    }
     */

    @PostMapping("/confirmarCorreo")
    public String confirmarCorreo(@ModelAttribute Usuario usuario, Model model) {
        try {
            // Verificar si el email está presente en la solicitud
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                System.err.println("El parámetro 'email' no está presente en la solicitud.");
                return "redirect:/error";
            }

            // Generar y enviar el código de verificación
            String codigoVerificacion = emailService.enviarCodigoVerificacion(usuario.getEmail());
            System.err.println(codigoVerificacion);

            // Almacenar el código de verificación junto con el usuario (pero no guardarlo en la base de datos)
            usuario.setCodigoVerificacion(codigoVerificacion);

            // Almacena el usuario en el modelo para que se pueda utilizar en la vista de confirmación
            model.addAttribute("usuario", usuario);
            model.addAttribute("email", usuario.getEmail());

            // Guardar el usuario en la base de datos
            //usuarioService.guardarUsuario(usuario);

            // Devolver el nombre de la vista en lugar de realizar una redirección
            return "confirmacionCorreo";
        } catch (Exception e) {
            // Manejar excepciones generales aquí, puedes registrar el error utilizando el sistema de registro de Spring
            // También puedes agregar un mensaje de error adicional al modelo si es necesario
            model.addAttribute("error", "Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
            return "error"; // Ajusta esto según tu manejo de errores
        }
    }

    //Metodo auxiliar, no se si esta bien

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@RequestParam("codigoVerificacion") String codigoVerificacion,
                                  @RequestParam("email") String email,
                                  Model model, @ModelAttribute Usuario usuario) {
        //email = usuario.getEmail();

        try {
            // Obtener el usuario por el correo
            //Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

            if (email != null) {
                String codigoAlmacenado = usuario.getCodigoVerificacion(); // Obtener el código almacenado

                if (codigoVerificacion.equals(codigoAlmacenado)) {
                    // Actualizar el estado de verificación y guardar el usuario en la base de datos después de la confirmación exitosa
                    usuario.setVerificado(true);
                    model.addAttribute("usuario", usuario);
                    System.err.println("El nombre del usuario es:"+usuario.getNombre());
                    System.err.println("El correo del usuario es:"+usuario.getEmail());
                    System.err.println("Los apellidos del usuario son:"+usuario.getApellidos());
                    System.err.println("La contraseña del usuario es:"+usuario.getPassword());
                    usuarioService.guardarUsuario(usuario);
                    model.addAttribute("confirmacionExitosa", true);
                    return "confirmacionCorreo";  // o la vista que uses para confirmación exitosa
                } else {
                    // Código de verificación incorrecto, muestra un mensaje de error en la misma vista.
                    model.addAttribute("error", "Código de verificación incorrecto. Debería ser: " + codigoAlmacenado);
                }
            } else {
                // Manejar el caso en que no se encuentra el usuario por el correo
                model.addAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            // Manejar excepciones generales aquí, puedes registrar el error utilizando el sistema de registro de Spring
            // También puedes agregar un mensaje de error adicional al modelo si es necesario
            model.addAttribute("error", "Error al procesar la solicitud. Por favor, inténtelo de nuevo.");
        }

        return "confirmacionCorreo";  // o la vista que usas para mostrar el formulario de confirmación
    }

    @GetMapping("/confirmacionCorreo")
    public String mostrarFormularioConfirmacion(@RequestParam("email") String email, Model model) {
        // Utiliza el usuario del modelo en lugar de crear uno nuevo
        Usuario usuario = (Usuario) model.getAttribute("usuario");

        if (usuario == null) {
            // Maneja el caso cuando el usuario es nulo, por ejemplo, redirigiendo a una página de error
            return "redirect:/error";
        }

        // Actualiza el email del usuario con el valor recibido
        usuario.setEmail(email);

        // Almacena el usuario actualizado en el modelo
        model.addAttribute("usuario", usuario);

        return "confirmacionCorreo";
    }

}
