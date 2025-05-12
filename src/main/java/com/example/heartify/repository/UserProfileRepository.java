package com.example.heartify.repository;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
    UserProfile findByUser(User user);
    List<UserProfile> findByKeywords_KeywordContainingIgnoreCase(String keyword);
}
