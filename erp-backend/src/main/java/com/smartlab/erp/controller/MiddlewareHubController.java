package com.smartlab.erp.controller;

import com.smartlab.erp.dto.MiddlewareAssetUpsertRequest;
import com.smartlab.erp.dto.MiddlewareRepositoryItemDTO;
import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.entity.MiddlewareAsset;
import com.smartlab.erp.service.MiddlewareHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/middleware-hub")
@RequiredArgsConstructor
public class MiddlewareHubController {

    private final MiddlewareHubService middlewareHubService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MiddlewareAsset>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "flowType", required = false) FlowType flowType) {
        return ResponseEntity.ok(middlewareHubService.list(keyword, flowType));
    }

    @GetMapping("/repository-view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MiddlewareRepositoryItemDTO>> repositoryView() {
        return ResponseEntity.ok(middlewareHubService.repositoryView());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiddlewareAsset> get(@PathVariable Long id) {
        return ResponseEntity.ok(middlewareHubService.get(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiddlewareAsset> create(@RequestBody MiddlewareAssetUpsertRequest request) {
        return ResponseEntity.ok(middlewareHubService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiddlewareAsset> update(@PathVariable Long id,
                                                  @RequestBody MiddlewareAssetUpsertRequest request) {
        return ResponseEntity.ok(middlewareHubService.update(id, request));
    }
}
