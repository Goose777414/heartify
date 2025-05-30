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
import java.util.Map;
import java.util.stream.Collectors;

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

        Map<Long, Long> senderProfileIds = invitations.stream()
                .collect(Collectors.toMap(
                        Invitation::getId,
                        inv -> profileRepository.findByUser(inv.getSender()).getId()
                ));
        model.addAttribute("senderProfileIds", senderProfileIds);

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

    /** Прийняти запрошення й перейти на профіль відправника */
    @GetMapping("/invitations/{id}/accept")
    public String acceptInvitation(@PathVariable Long id, HttpSession session) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }

        Invitation inv = invitationRepository.findById(id).orElse(null);
        if (inv != null
                && inv.getReceiver() != null
                && inv.getReceiver().getId().equals(current.getId())  // порівняння за id
                && !inv.isAccepted()
        ) {
            inv.setAccepted(true);
            invitationRepository.save(inv);

            // переходимо на профіль того, хто запросив
            User sender = inv.getSender();
            UserProfile sp = profileRepository.findByUser(sender);
            return "redirect:/profile/view/" + sp.getId();
        }

        return "redirect:/invitations";
    }

    /** Відхилити (видалити) запрошення */
    @GetMapping("/invitations/{id}/reject")
    public String rejectInvitation(@PathVariable Long id, HttpSession session) {
        User current = (User) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }

        Invitation inv = invitationRepository.findById(id).orElse(null);
        if (inv != null
                && inv.getReceiver() != null
                && inv.getReceiver().getId().equals(current.getId())  // порівняння за id
                && !inv.isAccepted()
        ) {
            invitationRepository.delete(inv);
        }
        return "redirect:/invitations";
    }
}
