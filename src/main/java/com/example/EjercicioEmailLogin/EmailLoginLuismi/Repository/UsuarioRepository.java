package com.example.EjercicioEmailLogin.EmailLoginLuismi.Repository;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    List<Usuario> findAll();
    Usuario findById(long id);
    List<Usuario> findByVerificado(boolean estaVerificado);

    Usuario deleteByEmail(String email);
}
