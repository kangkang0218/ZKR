package com.smartlab.erp.service;

import com.smartlab.erp.dto.ProjectChatMessageRequest;
import com.smartlab.erp.dto.ProjectChatMessageResponse;
import com.smartlab.erp.dto.ProjectChatParticipantResponse;
import com.smartlab.erp.entity.ProjectChatMessage;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.exception.PermissionDeniedException;
import com.smartlab.erp.repository.ProjectChatMessageRepository;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectChatService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final Set<String> ALLOWED_STAGE_TAGS = Set.of(
            "IDEA", "PROMOTION", "DEMO_EXECUTION", "MEETING_DECISION", "TESTING", "LAUNCHED", "SHELVED",
            "INITIATED", "TEAM_FORMATION", "IMPLEMENTING", "SETTLEMENT", "COMPLETED", "GENERAL"
    );

    private final ProjectChatMessageRepository projectChatMessageRepository;
    private final SysProjectRepository projectRepository;
    private final SysProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProjectChatMessageResponse> listMessages(String projectId, String userId, String stageTag) {
        assertMember(projectId, userId);
        String normalizedStageTag = normalizeStageTag(stageTag);
        List<ProjectChatMessage> source = normalizedStageTag == null
                ? projectChatMessageRepository.findTop100ByProjectIdOrderByIdDesc(projectId)
                : projectChatMessageRepository.findTop100ByProjectIdAndStageTagOrderByIdDesc(projectId, normalizedStageTag);
        return source.stream()
                .sorted(Comparator.comparing(ProjectChatMessage::getId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectChatMessageResponse postMessage(String projectId, String userId, ProjectChatMessageRequest request) {
        assertMember(projectId, userId);
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }
        User sender = userRepository.findById(userId).orElseThrow(() -> new BusinessException("当前用户不存在"));
        String senderName = sender.getName() == null || sender.getName().isBlank() ? sender.getUsername() : sender.getName();

        ProjectChatMessage saved = projectChatMessageRepository.save(ProjectChatMessage.builder()
                .projectId(projectId)
                .senderUserId(userId)
                .senderName(senderName)
                .content(content)
                .stageTag(normalizeStageTag(request.getStageTag()))
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectChatParticipantResponse> listParticipants(String projectId, String userId) {
        assertMember(projectId, userId);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(member -> ProjectChatParticipantResponse.builder()
                        .userId(member.getUser().getUserId())
                        .name(member.getUser().getName() == null || member.getUser().getName().isBlank() ? member.getUser().getUsername() : member.getUser().getName())
                        .role(member.getRole())
                        .avatar(member.getUser().getAvatar())
                        .hiddenAvatar(Boolean.TRUE.equals(member.getUser().getHiddenAvatar()))
                        .build())
                .toList();
    }

    private ProjectChatMessageResponse toResponse(ProjectChatMessage message) {
        return ProjectChatMessageResponse.builder()
                .id(message.getId())
                .projectId(message.getProjectId())
                .senderUserId(message.getSenderUserId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .stageTag(message.getStageTag())
                .createdAt(FORMATTER.format(message.getCreatedAt()))
                .build();
    }

    private String normalizeStageTag(String stageTag) {
        String normalized = stageTag == null ? "" : stageTag.trim().toUpperCase();
        if (normalized.isBlank()) {
            return null;
        }
        if (!ALLOWED_STAGE_TAGS.contains(normalized)) {
            throw new BusinessException("非法阶段标识: " + stageTag);
        }
        return normalized;
    }

    private void assertMember(String projectId, String userId) {
        projectRepository.findById(projectId).orElseThrow(() -> new BusinessException("项目不存在"));
        boolean isMember = projectMemberRepository.findByProjectIdAndUserUserId(projectId, userId).isPresent();
        if (!isMember) {
            throw new PermissionDeniedException("仅项目成员可访问团队聊天");
        }
    }
}
