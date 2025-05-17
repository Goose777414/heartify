package com.example.heartify.repository;

import com.example.heartify.model.Keyword;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface KeywordRepository extends CrudRepository<Keyword, Long> {
    Optional<Keyword> findByKeywordIgnoreCase(String keyword);
}
