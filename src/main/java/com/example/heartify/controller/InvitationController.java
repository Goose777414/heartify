package com.example.heartify.controller;

import com.example.heartify.model.Invitation;
import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Controller для роботи із запрошеннями між користувачами.
 */
@Controller
public class InvitationController {

    private final InvitationRepository invitationRepository;
    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Autowired
    public InvitationController(InvitationRepository invitationRepository,
                                UserProfileRepository profileRepository,
                                UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Список вхідних запрошень для поточного користувача
     */
    @GetMapping("/invitations")
    public String listInvitations(HttpSession session, Model model) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }
        // беремо всі запрошення, адресовані поточному користувачу
        List<Invitation> invitations = invitationRepository.findByReceiver(current);
        model.addAttribute("invitations", invitations);
        return "invitations";
    }

    /**
     * Ендпоінт відправки запрошення користувачу з указаним profileId
     */
    @GetMapping("/invitations/send/{profileId}")
    public String sendInvitation(@PathVariable Long profileId, HttpSession session) {
        User sender = (User) session.getAttribute("user");
        if (sender == null) {
            return "redirect:/login";
        }
        // знаходимо профіль отримувача
        UserProfile targetProfile = profileRepository.findById(profileId).orElse(null);
        if (targetProfile == null) {
            return "redirect:/home";
        }
        User receiver = targetProfile.getUser();

        // формуємо та зберігаємо запрошення
        Invitation invitation = new Invitation();
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setAccepted(false);
        invitationRepository.save(invitation);

        // повертаємося на сторінку перегляду профілю
        return "redirect:/profile/view/" + profileId;
    }
}
