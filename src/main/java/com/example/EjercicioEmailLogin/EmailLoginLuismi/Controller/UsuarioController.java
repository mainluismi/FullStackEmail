package com.example.EjercicioEmailLogin.EmailLoginLuismi.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {
    @GetMapping("/home")
    public String logUsuario(){
        return "home";
    }
}
