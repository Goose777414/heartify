package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.UserProfileRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileRepository profileRepository;

    @Autowired
    public ProfileController(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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

    @PostMapping("/create")
    public String processCreate(
            @ModelAttribute("profile") UserProfile form,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        form.setUser(user);
        UserProfile saved = profileRepository.save(form);
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
        // TODO: якщо у вас є keywords — оновити їх тут
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
        return "profile-view";
    }
}
