package com.movilidadsostenible.admin.services;

import com.movilidadsostenible.admin.models.entity.Admin;
import com.movilidadsostenible.admin.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Admin> listAdmins() {
        return (List<Admin>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> byId(String uid) {
        return repository.findById(uid);
    }

    @Override
    @Transactional
    public Admin save(Admin admin) {
        return repository.save(admin);
    }

    @Override
    @Transactional
    public void delete(String uid) {
        repository.deleteById(uid);
    }

}
