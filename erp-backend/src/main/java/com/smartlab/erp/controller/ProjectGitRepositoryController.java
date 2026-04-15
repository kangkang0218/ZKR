package com.smartlab.erp.controller;

import com.smartlab.erp.dto.*;
import com.smartlab.erp.security.UserPrincipal;
import com.smartlab.erp.service.ProjectGitRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/git-repositories")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProjectGitRepositoryController {

    private final ProjectGitRepositoryService projectGitRepositoryService;

    @GetMapping
    public ResponseEntity<List<ProjectGitRepositoryResponse>> list(@PathVariable String projectId,
                                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectGitRepositoryService.list(projectId, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<ProjectGitRepositoryResponse> create(@PathVariable String projectId,
                                                               @AuthenticationPrincipal UserPrincipal currentUser,
                                                               @RequestBody ProjectGitRepositoryCreateRequest request) {
        return ResponseEntity.ok(projectGitRepositoryService.create(projectId, currentUser.getId(), request));
    }

    @PostMapping("/{repoId}/test")
    public ResponseEntity<ProjectGitRepositoryTestResponse> test(@PathVariable String projectId,
                                                                 @PathVariable Long repoId,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectGitRepositoryService.test(projectId, repoId, currentUser.getId()));
    }

    @GetMapping("/{repoId}/logs")
    public ResponseEntity<List<ProjectGitCommitLogDTO>> logs(@PathVariable String projectId,
                                                             @PathVariable Long repoId,
                                                             @RequestParam(value = "limit", defaultValue = "30") int limit,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(projectGitRepositoryService.fetchCommitLogs(projectId, repoId, currentUser.getId(), limit));
    }
}
