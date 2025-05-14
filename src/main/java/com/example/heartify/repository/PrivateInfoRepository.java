package com.example.heartify.repository;

import com.example.heartify.model.PrivateInfo;
import com.example.heartify.model.UserProfile;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface PrivateInfoRepository extends CrudRepository<PrivateInfo, Long> {
    Optional<PrivateInfo> findByProfile(UserProfile profile);
}
