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

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam(name = "email", required = false, defaultValue = "") String email, Model model) {
        email = usuario.getEmail();
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

        // Almacenar el código de verificación junto con el usuario (pero no guardarlo en la base de datos)
        usuario.setCodigoVerificacion(codigoVerificacion);
        // Almacena el usuario en el modelo para que se pueda utilizar en la vista de confirmación
        model.addAttribute("usuario", usuario);
        model.addAttribute("email", email);

        // Devolver el nombre de la vista en lugar de realizar una redirección
        return "confirmacionCorreo";
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

    //Metodo auxiliar, no se si esta bien

    @PostMapping("/confirmarCorreo")
    public String confirmarCorreo(@RequestParam("codigoVerificacion") String codigoVerificacion,
                                  @RequestParam("email") String email,
                                  Model model) {
        try {
            // Obtener el usuario por el correo
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

            if (usuario != null) {
                String codigoAlmacenado = usuario.getCodigoVerificacion(); // Obtener el código almacenado

                if (codigoVerificacion.equals(codigoAlmacenado)) {
                    // Actualizar el estado de verificación y guardar el usuario en la base de datos después de la confirmación exitosa
                    usuario.setVerificado(true);
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

}
