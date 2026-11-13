package com.acme.portal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final AdminUserRepository userRepository;
    private final CurrentUserService currentUser;
    private final InternalAuditService auditService;
    private final Notifier notifier;

    public ProfileController(AdminUserRepository userRepository,
                             CurrentUserService currentUser,
                             InternalAuditService auditService,
                             Notifier notifier) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.notifier = notifier;
    }

    @GetMapping("/profile")
    public String profile(@RequestParam String userId) {
        currentUser.setUserId(userId);
        String profile = userRepository.findProfile(currentUser.getUserId());
        auditService.record(currentUser.getUserId(), "VIEW_PROFILE");
        return profile;
    }

    @PostMapping("/profile/notify")
    public String notifyUser(@RequestParam String userId, @RequestParam String message) {
        notifier.send(userId, message);
        return "sent to " + userId;
    }
}
