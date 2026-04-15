package com.smartlab.erp.command;

import com.smartlab.erp.command.dto.CommandExecuteRequest;
import com.smartlab.erp.command.dto.CommandExecuteResponse;
import com.smartlab.erp.command.dto.CommandInterpretRequest;
import com.smartlab.erp.command.dto.CommandPreviewResponse;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.exception.PermissionDeniedException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/command")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommandController {

    private final CommandService commandService;

    @PostMapping("/interpret")
    public ResponseEntity<CommandPreviewResponse> interpret(@RequestBody CommandInterpretRequest request) {
        return ResponseEntity.ok(commandService.interpret(request));
    }

    @PostMapping("/execute")
    public ResponseEntity<CommandExecuteResponse> execute(@RequestBody CommandExecuteRequest request) {
        return ResponseEntity.ok(commandService.execute(request));
    }

    @ExceptionHandler({BusinessException.class, PermissionDeniedException.class, IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        HttpStatus status = ex instanceof PermissionDeniedException ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }
}
