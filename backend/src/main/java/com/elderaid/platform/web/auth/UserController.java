package com.elderaid.platform.web.auth;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.web.dto.CurrentUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return new CurrentUserResponse(currentUser.id(), currentUser.email(), currentUser.roles());
    }
}
