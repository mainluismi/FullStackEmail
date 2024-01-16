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

        // Redirigir a la página de confirmación con el correo del usuario
        return "redirect:/confirmacionCorreo";
    }




    @GetMapping("/confirmacionCorreo")
    public String mostrarFormularioConfirmacion(@RequestParam("email") String email, Model model) {
        // Almacena el email en el modelo para que se pueda utilizar en la vista de confirmación
        model.addAttribute("usuario", new Usuario(email));
        return "confirmacionCorreo";
    }
    //Metodo auxiliar, no se si esta bien

    @PostMapping("/confirmarCorreo")
    public String confirmarCorreo(@RequestParam("codigoVerificacion") String codigoVerificacion, @RequestParam("email") String email, Model model) {
        // Obtener el usuario por el correo
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);
        System.err.println(codigoVerificacion);
        System.err.println(email);

        // Verificar el código de verificación
        if (usuario != null && codigoVerificacion.equals(usuario.getCodigoVerificacion())) {
            // Guardar el usuario en la base de datos después de la confirmación exitosa
            usuarioService.guardarUsuario(usuario);
            return "redirect:/home";
        } else {
            // Código de verificación incorrecto, muestra un mensaje de error en la misma vista.
            model.addAttribute("error", "Código de verificación incorrecto");
            return "confirmacionCorreo";
        }
    }




}
