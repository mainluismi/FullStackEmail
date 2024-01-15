package com.example.EjercicioEmailLogin.EmailLoginLuismi.Service;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private final UsuarioRepository usuaRioRepository;

    public UsuarioService(UsuarioRepository usuaRioRepository) {
        this.usuaRioRepository = usuaRioRepository;
    }

    public Usuario findByEmail(String email){
        return usuaRioRepository.findByEmail(email);
    }

    public Usuario obtenerUsuarioPorId(Long id){
        return usuaRioRepository.findById(id).orElse(null);
    }

    public List<Usuario> obtenerTodosLosUsuarios(){
        return usuaRioRepository.findAll();
    }

    public List<Usuario> obtenerUsuariosVerificados(boolean estaVerificado){
        return usuaRioRepository.findByVerificado(estaVerificado);
    }

    public Usuario guardarUsuario(Usuario usuario){
        return usuaRioRepository.save(usuario);
    }
}
