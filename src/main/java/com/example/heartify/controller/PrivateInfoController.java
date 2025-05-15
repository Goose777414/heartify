package com.example.heartify.controller;

import com.example.heartify.model.PrivateInfo;
import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.PrivateInfoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * Controller to handle viewing, creating, and editing of private information
 * only after a user has an accepted invitation.
 */
@Controller
public class PrivateInfoController {

    private final InvitationRepository invitationRepository;
    private final UserProfileRepository profileRepository;
    private final PrivateInfoRepository privateInfoRepository;

    @Autowired
    public PrivateInfoController(InvitationRepository invitationRepository,
                                 UserProfileRepository profileRepository,
                                 PrivateInfoRepository privateInfoRepository) {
        this.invitationRepository = invitationRepository;
        this.profileRepository = profileRepository;
        this.privateInfoRepository = privateInfoRepository;
    }

    /**
     * View private information for a given profile ID.
     */
    @GetMapping("/private-info/{profileId}")
    public String viewPrivateInfo(@PathVariable Long profileId,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile targetProfile = profileRepository.findById(profileId).orElse(null);
        if (targetProfile == null) {
            return "redirect:/home";
        }
        boolean canView = invitationRepository
                .existsBySenderAndReceiverAndAccepted(user, targetProfile.getUser(), true);
        if (!canView) {
            return "redirect:/invitations";
        }
        Optional<PrivateInfo> infoOpt = privateInfoRepository.findByProfile(targetProfile);
        if (infoOpt.isEmpty()) {
            // Якщо приватної інформації немає
            return "redirect:/profile";
        }
        model.addAttribute("info", infoOpt.get());
        return "private-info";
    }

    /**
     * Show form to create private information if none exists.
     */
    @GetMapping("/private-info/create")
    public String showCreatePrivateInfo(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            return "redirect:/profile/create";
        }
        Optional<PrivateInfo> existing = privateInfoRepository.findByProfile(profile);
        if (existing.isPresent()) {
            return "redirect:/private-info/edit";
        }
        model.addAttribute("privateInfo", new PrivateInfo());
        return "private-info-create";
    }

    /**
     * Process creation of private information.
     */
    @PostMapping("/private-info/create")
    public String processCreatePrivateInfo(
            @ModelAttribute("privateInfo") PrivateInfo form,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            return "redirect:/profile/create";
        }
        form.setProfile(profile);
        privateInfoRepository.save(form);
        return "redirect:/private-info/" + profile.getId();
    }

    /**
     * Show form to edit private information if exists, else redirect to create.
     */
    @GetMapping("/private-info/edit")
    public String showEditPrivateInfo(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            return "redirect:/profile/create";
        }
        Optional<PrivateInfo> infoOpt = privateInfoRepository.findByProfile(profile);
        if (infoOpt.isEmpty()) {
            return "redirect:/private-info/create";
        }
        model.addAttribute("privateInfo", infoOpt.get());
        return "private-info-edit";
    }

    /**
     * Process submission of the private information form (edit).
     */
    @PostMapping("/private-info/edit")
    public String processEditPrivateInfo(
            @ModelAttribute("privateInfo") PrivateInfo form,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        UserProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            return "redirect:/profile/create";
        }
        form.setProfile(profile);
        privateInfoRepository.save(form);
        return "redirect:/private-info/" + profile.getId();
    }
}
