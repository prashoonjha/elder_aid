package com.elderaid.platform.web.elderly;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.ElderlyProfileService;
import com.elderaid.platform.web.dto.CreateElderlyProfileRequest;
import com.elderaid.platform.web.dto.ElderlyProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/elderly-profiles")
public class ElderlyProfileController {

    private final ElderlyProfileService elderlyProfileService;

    public ElderlyProfileController(ElderlyProfileService elderlyProfileService) {
        this.elderlyProfileService = elderlyProfileService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public ResponseEntity<ElderlyProfileResponse> create(
            @AuthenticationPrincipal CurrentUser caller,
            @Valid @RequestBody CreateElderlyProfileRequest request
    ) {
        ElderlyProfileResponse response = elderlyProfileService.createProfile(caller.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public List<ElderlyProfileResponse> mine(@AuthenticationPrincipal CurrentUser caller) {
        return elderlyProfileService.listMine(caller.id());
    }
}
