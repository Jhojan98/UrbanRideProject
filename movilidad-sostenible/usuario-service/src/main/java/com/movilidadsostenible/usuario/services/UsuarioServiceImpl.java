package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return (List<User>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> byId(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<User> byUserEmail(String userEmail) {
        return repository.findByUserEmail(userEmail);
    }

    @Override
    public void updateVerificationStatus(Integer userCc, boolean verified) {
        Optional<User> optionalUser = repository.findById(userCc);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setIsVerified(verified);
            repository.save(user);
        }
    }
}
