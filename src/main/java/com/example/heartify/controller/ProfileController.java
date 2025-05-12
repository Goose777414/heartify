package com.example.heartify.controller;

import com.example.heartify.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {
    @Autowired
    private UserProfileRepository profileRepository;

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        // 1) Перевіряємо, чи є в сесії залогінений користувач
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // 2) Підтягуємо його анкету, або створюємо за замовчуванням
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        // 3) Кладемо в модель, щоб Thymeleaf заповнив форму
        model.addAttribute("profile", profile);
        return "profile-edit";  // повертаємо profile-edit.html
    }

    @PostMapping("/profile/edit")
    public String saveProfile(
            @ModelAttribute("profile") UserProfile updatedProfile,
            HttpSession session,
            Model model
    ) {
        // 1. Перевіряємо сесію
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // 2. Підтягуємо існуючий профіль користувача
        UserProfile existingProfile = profileRepository.findByUser(user);

        if (existingProfile == null) {
            // Якщо профіль не знайдено — створюємо новий
            existingProfile = new UserProfile();
            existingProfile.setUser(user);
        }

        // 3. Оновлюємо лише потрібні поля
        existingProfile.setName(updatedProfile.getName());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setCity(updatedProfile.getCity());
        existingProfile.setAbout(updatedProfile.getAbout());

        // 4. Зберігаємо в базу
        profileRepository.save(existingProfile);

        // 5. Повертаємо на домашню сторінку
        return "redirect:/home";
    }


    // Показати форму створення анкети
    @GetMapping("/profile/create")
    public String showCreateProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // кладемо порожній обʼєкт, щоб th:object запрацював
        model.addAttribute("profile", new UserProfile());
        return "profile-create";   // <— імʼя вашого шаблону profile-create.html
    }

    // Обробка сабміту форми створення анкети
    @PostMapping("/profile/create")
    public String saveNewProfile(@ModelAttribute("profile") UserProfile profile,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        profile.setUser(user);
        profileRepository.save(profile);
        return "redirect:/home";
    }


    @GetMapping("/profile/view/{id}")
    public String viewProfile(@PathVariable("id") Long id, Model model) {
        UserProfile profile = profileRepository.findById(id).orElse(null);

        if (profile == null) {
            return "redirect:/home"; // Якщо профілю немає — назад на головну
        }

        model.addAttribute("profile", profile);
        return "profile-view"; // Це має бути твій новий файл profile-view.html
    }
}
