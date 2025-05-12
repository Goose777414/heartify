package com.example.heartify.repository;

import com.example.heartify.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository  extends CrudRepository<User, Long> {
    User findByUsername(String username);
}

