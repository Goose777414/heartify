package com.example.heartify.controller;

import com.example.heartify.model.User;
import com.example.heartify.model.UserProfile;
import com.example.heartify.model.PrivateInfo;
import com.example.heartify.repository.InvitationRepository;
import com.example.heartify.repository.UserProfileRepository;
import com.example.heartify.repository.PrivateInfoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
     * Only accessible if there's an accepted invitation.
     */
    @GetMapping("/private-info/{profileId}")
    public String viewPrivateInfo(@PathVariable Long profileId,
                                  HttpSession session,
                                  Model model) {
        // 1. Check session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // 2. Load own and target profiles
        UserProfile ownProfile = profileRepository.findByUser(user);
        UserProfile targetProfile = profileRepository.findById(profileId).orElse(null);
        if (targetProfile == null) {
            return "redirect:/home";
        }

        // 3. Verify accepted invitation
        User targetUser = targetProfile.getUser();
        boolean canView = invitationRepository
                .existsBySenderAndReceiverAndAccepted(user, targetUser, true);
        if (!canView) {
            return "redirect:/invitations";
        }

        // 4. Fetch private info
        PrivateInfo info = privateInfoRepository
                .findByProfile(targetProfile)
                .orElseThrow(() -> new IllegalStateException("Private info not found for profile: " + profileId));

        // 5. Add to model and render
        model.addAttribute("info", info);
        return "private-info";
    }

    @GetMapping("/private-info/edit")
    public String showEditPrivateInfo(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        UserProfile profile = profileRepository.findByUser(user);
        PrivateInfo info = privateInfoRepository
                .findByProfile(profile)
                .orElse(new PrivateInfo());
        model.addAttribute("privateInfo", info);
        return "private-info-edit";
    }

    @PostMapping("/private-info/edit")
    public String processEditPrivateInfo(
            @ModelAttribute("privateInfo") PrivateInfo form,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        UserProfile profile = profileRepository.findByUser(user);
        form.setProfile(profile);
        privateInfoRepository.save(form);
        return "redirect:/profile";
    }
}
