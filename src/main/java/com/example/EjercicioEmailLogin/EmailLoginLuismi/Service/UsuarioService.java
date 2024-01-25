package com.example.EjercicioEmailLogin.EmailLoginLuismi.Service;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import com.example.EjercicioEmailLogin.EmailLoginLuismi.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
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

    public Usuario deleteByEmail(String email){
        return usuaRioRepository.deleteByEmail(email);
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

    public Usuario obtenerUsuarioPorEmail(String email) {
        // Implementa la lógica para obtener un usuario por su correo electrónico
        return usuaRioRepository.findByEmail(email);
    }

    public Usuario guardarUsuario(Usuario usuario){
        return usuaRioRepository.save(usuario);
    }

    public Usuario autenticarUsuario(String email, String password) {
        // Busca al usuario por su correo electrónico en la base de datos
        Usuario usuario = usuaRioRepository.findByEmail(email);

        // Verifica si el usuario existe y la contraseña coincide
        if (usuario != null && usuario.getPassword().equals(password)) {
            return usuario; // Usuario autenticado con éxito
        } else {
            return null; // Autenticación fallida
        }
    }

    public boolean verificarAdmin(String email, String password){
        if(email.equals("admin@admin.com") && password.equals("admin")){
            return true;
        }
        return false;
    }

    @Transactional
    public void eliminarUsuarioPorEmail(String email) {
        // Buscar el usuario por su email
        Usuario usuario = usuaRioRepository.findByEmail(email);

        // Verificar si el usuario existe antes de intentar eliminarlo
        if (usuario != null) {
            usuaRioRepository.delete(usuario);
        }
    }
}
