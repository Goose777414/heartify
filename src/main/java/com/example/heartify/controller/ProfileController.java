package com.example.heartify.controller;

import com.example.heartify.model.Keyword;
import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.KeywordRepository;
import com.example.heartify.repository.PrivateInfoRepository;
import com.example.heartify.repository.UserProfileRepository;
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
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/login";

        UserProfile myProfile = profileRepository.findByUser(me);
        if (myProfile == null) return "redirect:/profile/create";

        model.addAttribute("profile", myProfile);
        model.addAttribute("pageTitle", "Мій профіль");
        model.addAttribute("showInviteButton", false);
        model.addAttribute("showEditPrivateLink", true);
        // якщо приватна інформація вже є
        boolean hasInfo = privateInfoRepository.findByProfile(myProfile).isPresent();
        model.addAttribute("showPrivateInfoLink", hasInfo);

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
    public String listAllProfiles(HttpSession session, Model model) {
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
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/login";

        UserProfile other = profileRepository.findById(id).orElse(null);
        if (other == null) return "redirect:/home";

        model.addAttribute("profile", other);
        model.addAttribute("pageTitle", "Профіль " + other.getName() + " користувача");
        model.addAttribute("showEditPrivateLink", false);

        // чи вже надсилали запрошення саме цьому користувачу?
        boolean alreadySent = invitationRepository
                .findBySenderAndReceiver(me, other.getUser())
                .isPresent();
        model.addAttribute("showInviteButton", !alreadySent);

        // якщо ви надіслали й це запрошення прийняли
        boolean accepted = invitationRepository
                .existsBySenderAndReceiverAndAccepted(me, other.getUser(), true);
        model.addAttribute("showPrivateInfoLink", accepted);

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
