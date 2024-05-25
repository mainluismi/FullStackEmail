package com.example.EjercicioEmailLogin.EmailLoginLuismi.Repository;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByEmail(String email);
    void deleteById(Long id);
    Admin findById(long id);
}
