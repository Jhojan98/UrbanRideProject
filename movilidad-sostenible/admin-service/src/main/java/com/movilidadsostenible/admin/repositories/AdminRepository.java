package com.movilidadsostenible.admin.repositories;

import com.movilidadsostenible.admin.models.entity.Admin;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, String> {
}
