package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.model.Keyword;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.KeywordRepository;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.PrivateInfoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller для роботи з профілями користувачів.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileRepository profileRepository;
    private final KeywordRepository keywordRepository;
    private final InvitationRepository invitationRepository;
    private final PrivateInfoRepository privateInfoRepository;

    @Autowired
    public ProfileController(UserProfileRepository profileRepository,
                             KeywordRepository keywordRepository,
                             InvitationRepository invitationRepository,
                             PrivateInfoRepository privateInfoRepository) {
        this.profileRepository = profileRepository;
        this.keywordRepository = keywordRepository;
        this.invitationRepository = invitationRepository;
        this.privateInfoRepository = privateInfoRepository;
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
            return "redirect:/profile/create";
        }
        model.addAttribute("profile", profile);
        // Покажемо кнопку редагування приватної інформації власнику
        model.addAttribute("showEditPrivateLink", true);
        model.addAttribute("showInviteButton", false);
        // Перевірка наявності приватної інформації для показу
        boolean hasPrivateInfo = privateInfoRepository.findByProfile(profile).isPresent();
        model.addAttribute("showPrivateInfoLink", hasPrivateInfo);
        return "profile-view";
    }

    // ==== 2. СВОЯ АНКЕТА: створення ====
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("profile", new UserProfile());
        return "profile-create";
    }

    @PostMapping("/create")
    public String processCreate(
            @ModelAttribute("profile") UserProfile form,
            @RequestParam(value = "keywordsStr", required = false) String keywordsStr,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Keyword> kws = parseKeywords(keywordsStr);
        form.setUser(user);
        form.setKeywords(kws);
        profileRepository.save(form);
        return "redirect:/profile";
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
            return "redirect:/profile/create";
        }
        String keywordsStr = profile.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.joining(","));
        model.addAttribute("profile", profile);
        model.addAttribute("keywordsStr", keywordsStr);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String processEdit(
            @ModelAttribute("profile") UserProfile form,
            @RequestParam(value = "keywordsStr", required = false) String keywordsStr,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile existing = profileRepository.findByUser(user);
        if (existing == null) {
            existing = new UserProfile();
            existing.setUser(user);
        }
        existing.setName(form.getName());
        existing.setAge(form.getAge());
        existing.setCity(form.getCity());
        existing.setAbout(form.getAbout());
        List<Keyword> kws = parseKeywords(keywordsStr);
        existing.setKeywords(kws);
        profileRepository.save(existing);
        return "redirect:/profile";
    }

    // ==== 4. Список усіх анкет ====
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

    // ==== 5. ЧУЖА АНКЕТА: перегляд за ID ====
    @GetMapping("/view/{id}")
    public String viewOtherProfile(@PathVariable Long id,
                                   HttpSession session,
                                   Model model) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return "redirect:/home";
        }
        model.addAttribute("profile", profile);
        boolean hasInvitation = invitationRepository
                .findBySenderAndReceiver(current, profile.getUser())
                .isPresent();
        model.addAttribute("showInviteButton", !hasInvitation);
        boolean accepted = invitationRepository
                .existsBySenderAndReceiverAndAccepted(current, profile.getUser(), true);
        model.addAttribute("showPrivateInfoLink", accepted);
        model.addAttribute("showEditPrivateLink", false);
        return "profile-view";
    }

    /**
     * Допоміжний метод: розбирає рядок ключових слів та повертає List<Keyword>.
     */
    private List<Keyword> parseKeywords(String keywordsStr) {
        if (keywordsStr == null || keywordsStr.isBlank()) {
            return List.of();
        }
        return Arrays.stream(keywordsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> keywordRepository.findByKeywordIgnoreCase(s)
                        .orElseGet(() -> {
                            Keyword k = new Keyword();
                            k.setKeyword(s);
                            return keywordRepository.save(k);
                        }))
                .collect(Collectors.toList());
    }
}
