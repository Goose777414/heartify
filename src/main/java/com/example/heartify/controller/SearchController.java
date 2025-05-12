package com.example.heartify.controller;

import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private UserProfileRepository profileRepository;

    @GetMapping("/search")
    public String searchProfiles(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<UserProfile> profiles = new ArrayList<>();

        if (keyword == null || keyword.isBlank()) {
            profileRepository.findAll().forEach(profiles::add); // вручну додаємо в список
        } else {
            profiles = profileRepository.findByKeywords_KeywordContainingIgnoreCase(keyword);
        }

        model.addAttribute("profiles", profiles);
        return "search";
    }
}
