package com.movilidadsostenible.admin.services;

import com.movilidadsostenible.admin.models.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<Admin> listAdmins();
    Admin save(Admin admin);
    void delete(String uid);
    Optional<Admin> byId(String uidUser);
}
