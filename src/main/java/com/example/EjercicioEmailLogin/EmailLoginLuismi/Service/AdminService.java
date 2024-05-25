package com.example.EjercicioEmailLogin.EmailLoginLuismi.Service;

import com.example.EjercicioEmailLogin.EmailLoginLuismi.Repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
}
