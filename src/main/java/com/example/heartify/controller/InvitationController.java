package com.example.heartify.controller;

import com.example.heartify.model.Invitation;
import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.UserProfileRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * Controller для роботи із запрошеннями між користувачами.
 */
@Controller
public class InvitationController {

    private final InvitationRepository invitationRepository;
    private final UserProfileRepository profileRepository;

    @Autowired
    public InvitationController(InvitationRepository invitationRepository,
                                UserProfileRepository profileRepository) {
        this.invitationRepository = invitationRepository;
        this.profileRepository = profileRepository;
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
        List<Invitation> invitations = invitationRepository.findByReceiver(current);
        model.addAttribute("invitations", invitations);
        return "invitations";
    }

    /**
     * Відправка запрошення від поточного користувача до профілю з profileId.
     * Перевіряє наявність дублікату.
     */
    @GetMapping("/invitations/send/{profileId}")
    public String sendInvitation(@PathVariable Long profileId, HttpSession session) {
        User sender = (User) session.getAttribute("user");
        if (sender == null) {
            return "redirect:/login";
        }
        UserProfile targetProfile = profileRepository.findById(profileId).orElse(null);
        if (targetProfile == null) {
            return "redirect:/home";
        }
        User receiver = targetProfile.getUser();

        // Не дозволяємо дублікати
        Optional<Invitation> existing = invitationRepository.findBySenderAndReceiver(sender, receiver);
        if (existing.isEmpty()) {
            Invitation invitation = new Invitation();
            invitation.setSender(sender);
            invitation.setReceiver(receiver);
            invitation.setAccepted(false);
            invitationRepository.save(invitation);
        }
        return "redirect:/profile/view/" + profileId;
    }

    /**
     * Прийняти запрошення — відправляє на приватну інформацію відправника.
     */
    @GetMapping("/invitations/{id}/accept")
    public String acceptInvitation(@PathVariable Long id, HttpSession session) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }
        Optional<Invitation> invOpt = invitationRepository.findById(id);
        if (invOpt.isPresent()) {
            Invitation inv = invOpt.get();
            if (inv.getReceiver().equals(current) && !inv.isAccepted()) {
                inv.setAccepted(true);
                invitationRepository.save(inv);
                // Переходимо на перегляд приватної інформації відправника
                User sender = inv.getSender();
                UserProfile senderProfile = profileRepository.findByUser(sender);
                return "redirect:/private-info/" + senderProfile.getId();
            }
        }
        return "redirect:/invitations";
    }

    /**
     * Відхилити (видалити) запрошення
     */
    @GetMapping("/invitations/{id}/reject")
    public String rejectInvitation(@PathVariable Long id, HttpSession session) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }
        Optional<Invitation> invOpt = invitationRepository.findById(id);
        if (invOpt.isPresent()) {
            Invitation inv = invOpt.get();
            if (inv.getReceiver().equals(current) && !inv.isAccepted()) {
                invitationRepository.delete(inv);
            }
        }
        return "redirect:/invitations";
    }
}
