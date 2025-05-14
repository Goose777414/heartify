package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.model.Keyword;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.KeywordRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileRepository profileRepository;
    private final KeywordRepository keywordRepository;

    @Autowired
    public ProfileController(UserProfileRepository profileRepository,
                             KeywordRepository keywordRepository) {
        this.profileRepository = profileRepository;
        this.keywordRepository = keywordRepository;
    }

    // ==== 1. СВОЯ АНКЕТА: перегляд ====
    @GetMapping
    public String viewOwnProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            // якщо профілю ще нема — перенаправимо на створення
            return "redirect:/profile/create";
        }
        model.addAttribute("profile", profile);
        return "profile-view";   // той самий шаблон, що й для чужих анкет
    }

    // ==== 2. СВОЯ АНКЕТА: створення ====
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // даємо порожній об’єкт у форму
        model.addAttribute("profile", new UserProfile());
        return "profile-create";
    }
    @GetMapping("/all")
    public String listAllProfiles(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Iterable<UserProfile> profiles = profileRepository.findAll();
        model.addAttribute("profiles", profiles);
        return "profiles";
    }
    @PostMapping("/create")
    public String processCreate(
            @ModelAttribute("profile") UserProfile form,
            @RequestParam(value = "keywordsStr", required = false) String keywordsStr,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Keyword> kws = parseKeywords(keywordsStr);
        form.setUser(user);
        form.setKeywords(kws);
        UserProfile saved = profileRepository.save(form);
        profileRepository.save(form);
        return "redirect:/profile";  // після створення — на свій профіль
    }

    // ==== 3. СВОЯ АНКЕТА: редагування ====
    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            // якщо немає — створити
            return "redirect:/profile/create";
        }
        model.addAttribute("profile", profile);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String processEdit(
            @ModelAttribute("profile") UserProfile form,
            @RequestParam(value = "keywordsStr", required = false) String keywordsStr,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile existing = profileRepository.findByUser(user);
        if (existing == null) {
            // на всякий випадок
            existing = new UserProfile();
            existing.setUser(user);
        }
        // оновлюємо поля
        existing.setName(form.getName());
        existing.setAge(form.getAge());
        existing.setCity(form.getCity());
        existing.setAbout(form.getAbout());

        List<Keyword> kws = parseKeywords(keywordsStr);
        existing.setKeywords(kws);

        profileRepository.save(existing);
        return "redirect:/profile";
    }

    // ==== 4. ЧУЖА АНКЕТА: перегляд за ID ====
    @GetMapping("/view/{id}")
    public String viewOtherProfile(@PathVariable Long id, Model model) {
        UserProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return "redirect:/home";
        }
        model.addAttribute("profile", profile);
        model.addAttribute("showInviteButton", true);
        return "profile-view";
    }

private List<Keyword> parseKeywords(String keywordsStr) {
    if (keywordsStr == null || keywordsStr.isBlank()) {
        return List.of();
    }
    return Arrays.stream(keywordsStr.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> {
                Optional<Keyword> existing = keywordRepository.findByKeywordIgnoreCase(s);
                return existing.orElseGet(() -> {
                    Keyword k = new Keyword();
                    k.setKeyword(s);
                    return keywordRepository.save(k);
                });
            })
            .collect(Collectors.toList());
}
}