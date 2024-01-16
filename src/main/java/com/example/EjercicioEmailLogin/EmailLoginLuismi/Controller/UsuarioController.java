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
    public String guardarUsuario(@ModelAttribute Usuario usuario){
        String codigoVerificacion = emailService.enviarCodigoVerificacion(usuario.getEmail());
       codigoAlmacenado = codigoVerificacion;
        Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
        return "redirect:/home";
    }

    @GetMapping("/confirmacionCorreo")
    public String mostrarFormularioConfirmacion(Model model) {
        // Puedes generar y almacenar un código de verificación en tu lógica de negocio aquí
        // y luego pasarlo al modelo para que se muestre en el formulario.
        model.addAttribute("codigoVerificacion", codigoAlmacenado); // Reemplaza esto con tu lógica de generación de código.

        return "confirmacionCorreo";
    }
    //Metodo auxiliar, no se si esta bien

    @PostMapping("/confirmarCorreo")
    public String confirmarCorreo(@RequestParam("codigoVerificacion") String codigoVerificacion, Model model) {
        // Aquí puedes validar el código de verificación y realizar las acciones necesarias.
        // Puedes utilizar el servicio de usuario para activar la cuenta o realizar otras operaciones.

        if (codigoAlmacenado.equals(codigoVerificacion)) { // Reemplaza con tu lógica de validación.
            // Acciones después de la confirmación exitosa (por ejemplo, activar la cuenta).
            System.out.println("Verificacion CORRECTA");
            return "redirect:/home";
        } else {
            // Código de verificación incorrecto, muestra un mensaje de error en la misma vista.
            model.addAttribute("error", "Código de verificación incorrecto");
            model.addAttribute("codigoVerificacion", codigoVerificacion); // Puedes volver a mostrar el código ingresado
            System.out.println("Verificacion INCORRECTA");

            return "confirmacionCorreo";
        }
    }


}
