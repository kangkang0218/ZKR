package com.smartlab.erp.controller;

import com.smartlab.erp.dto.ProjectChatMessageRequest;
import com.smartlab.erp.dto.ProjectChatMessageResponse;
import com.smartlab.erp.dto.ProjectChatParticipantResponse;
import com.smartlab.erp.security.UserPrincipal;
import com.smartlab.erp.service.ProjectChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
@RequiredArgsConstructor
public class ProjectChatController {

    private final ProjectChatService projectChatService;

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectChatMessageResponse>> listMessages(@PathVariable String projectId,
                                                                         @RequestParam(value = "stageTag", required = false) String stageTag,
                                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectChatService.listMessages(projectId, currentUser.getId(), stageTag));
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectChatMessageResponse> postMessage(@PathVariable String projectId,
                                                                   @Valid @RequestBody ProjectChatMessageRequest request,
                                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectChatService.postMessage(projectId, currentUser.getId(), request));
    }

    @GetMapping("/participants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectChatParticipantResponse>> listParticipants(@PathVariable String projectId,
                                                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectChatService.listParticipants(projectId, currentUser.getId()));
    }
}
