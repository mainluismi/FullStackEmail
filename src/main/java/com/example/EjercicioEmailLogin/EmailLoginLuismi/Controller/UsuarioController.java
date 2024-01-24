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
    public String logUsuario(Model model){
        model.addAttribute("titulo","Registro de Usuario");
        return "home";
    }

    @PostMapping("/formularioRegistro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "formularioRegistro";
    }

    @PostMapping("/confirmarCorreo")
    public String confirmarCorreo(@ModelAttribute Usuario usuario, Model model) {
        try {
            // Verificar si el email está presente en la solicitud
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                System.err.println("El parámetro 'email' no está presente en la solicitud.");
                return "redirect:/error";
            }

            // Verificar si el email ya existe en la base de datos
            Usuario usuarioExistente = usuarioService.findByEmail(usuario.getEmail());

            if (usuarioExistente != null) {
                // Si el email ya existe, mostrar un mensaje de error y redirigir de nuevo al formulario de registro
                System.err.println("No se pudo guardar, email existente");
                model.addAttribute("error", "Error: el email ya existe");
                return "formularioRegistro";
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
                    return "redirect:/home";
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

        return "redirect:/home";
    }

    @PostMapping("/inicioSesion")
    public String iniciarSesion(@RequestParam("email") String email,
                                @RequestParam("password") String password,
                                Model model) {
        //Si el usuario se registra con admin y admin puede tener acceso a los datos de los usuarios
        if (usuarioService.verificarAdmin(email, password)){
            return "admin";
        }
        // Intenta autenticar al usuario usando el servicio de usuario
        Usuario usuarioAutenticado = usuarioService.autenticarUsuario(email, password);

        if (usuarioAutenticado != null) {
            // Si el usuario está autenticado, puedes redirigirlo a la página de inicio de sesión exitosa
            model.addAttribute("usuario", usuarioAutenticado);
            return "inicioSesion";
        } else {
            // Si la autenticación falla, puedes mostrar un mensaje de error en la página de inicio
            model.addAttribute("titulo", "Correo o contraseña incorrectos");
            return "home";
        }
    }

}
