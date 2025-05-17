package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.UserProfileRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Controller for searching user profiles by keyword.
 * Excludes the current user's own profile from results.
 */
@Controller
public class SearchController {

    private final UserProfileRepository profileRepository;

    @Autowired
    public SearchController(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping("/search")
    public String searchProfiles(@RequestParam(value = "keyword", required = false) String keyword,
                                 HttpSession session,
                                 Model model) {
        // 1) Збираємо результати пошуку
        List<UserProfile> profiles = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            profileRepository.findAll().forEach(profiles::add);
        } else {
            profiles = profileRepository.findByKeywords_KeywordContainingIgnoreCase(keyword);
        }

        // 2) Прибираємо власний профіль з результатів пошуку
        User current = (User) session.getAttribute("user");
        if (current != null) {
            UserProfile own = profileRepository.findByUser(current);
            if (own != null) {
                profiles = profiles.stream()
                        .filter(p -> !p.getId().equals(own.getId()))
                        .collect(Collectors.toList());
            }
        }

        // 3) Віддаємо результати в модель
        model.addAttribute("profiles", profiles);

        return "search";
    }
}
