package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {

        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);

            // якщо анкети нема — на створення
            UserProfile profile = profileRepository.findByUser(user);
            if (profile == null) {
                return "redirect:/profile/create";
            }
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Невірний логін або пароль");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           HttpSession session,
                           Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Паролі не співпадають");
            return "register";
        }

        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("error", "Користувач з таким іменем вже існує");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userRepository.save(newUser);

        session.setAttribute("user", newUser);

        // після реєстрації також перевіряємо наявність анкети
        UserProfile profile = profileRepository.findByUser(newUser);
        if (profile == null) {
            return "redirect:/profile/create";
        }
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String showHomePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // 1) Підтягуємо свій профіль із БД
        UserProfile ownProfile = profileRepository.findByUser(user);

        // 2) Вітання — тепер своє реальне ім'я (як його зберігаєш у UserProfile)
        model.addAttribute("userName", ownProfile.getName());

        // 3) Список чужих анкет — беремо не User, а UserProfile
        Iterable<UserProfile> allProfiles = profileRepository.findAll();
        // якщо хочеш прибрати себе зі списку, можеш відфільтрувати:
        // List<UserProfile> others = StreamSupport.stream(allProfiles.spliterator(), false)
        //     .filter(p -> !p.getId().equals(ownProfile.getId()))
        //     .collect(Collectors.toList());
        // model.addAttribute("profiles", others);
        model.addAttribute("profiles", allProfiles);

        return "home";

    }
}
