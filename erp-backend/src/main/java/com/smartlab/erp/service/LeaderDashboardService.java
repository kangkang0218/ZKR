package com.smartlab.erp.service;

import com.smartlab.erp.dto.LeaderDashboardResponse;
import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.SysProjectMember;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.entity.UserRole;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import com.smartlab.erp.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderDashboardService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final SysProjectMemberRepository projectMemberRepository;
    private final SysProjectRepository projectRepository;

    public boolean isLeader(String userId, String role) {
        return userRoleRepository.existsByUserIdAndRoleAndIsLeaderTrue(userId, role.toUpperCase());
    }


    @Transactional(readOnly = true)
    public LeaderDashboardResponse getLeaderDashboard(String leaderUserId, String role) {
        String normalizedRole = role.toUpperCase();

        if (!isLeader(leaderUserId, normalizedRole)) {
            throw new RuntimeException("无权访问: 用户不是该角色的队长");
        }

        User leader = userRepository.findById(leaderUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<SysProjectMember> leaderProjectMembers = projectMemberRepository.findByUserIdWithUser(leaderUserId);

        List<String> leaderProjectIds = leaderProjectMembers.stream()
                .map(SysProjectMember::getProjectId)
                .distinct()
                .collect(Collectors.toList());

        if (leaderProjectIds.isEmpty()) {
            return LeaderDashboardResponse.builder()
                    .leaderRole(normalizedRole)
                    .leaderName(leader.getName())
                    .members(new ArrayList<>())
                    .totalMembers(0)
                    .activeProjects(0)
                    .totalWeight(0)
                    .totalEstimatedCost(0.0)
                    .build();
        }

        List<SysProjectMember> allProjectMembers = projectMemberRepository.findByProjectIdInWithUser(leaderProjectIds);

        Map<String, List<SysProjectMember>> membersByUserId = allProjectMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getUser().getUserId()));

        List<LeaderDashboardResponse.MemberProjectInfo> memberInfos = membersByUserId.values().stream()
                .map(projectMembers -> {
                    User user = projectMembers.get(0).getUser();
                    return buildMemberProjectInfo(user, projectMembers, leaderProjectIds);
                })
                .collect(Collectors.toList());

        int totalMembers = memberInfos.size();

        Set<String> activeProjectIds = new HashSet<>();
        int totalWeight = 0;
        double totalEstimatedCost = 0.0;

        for (LeaderDashboardResponse.MemberProjectInfo member : memberInfos) {
            totalWeight += member.getTotalWeight();
            totalEstimatedCost += member.getEstimatedCost();

            for (LeaderDashboardResponse.ProjectParticipation project : member.getProjects()) {
                String status = project.getStatus();
                if (isActiveStatus(status)) {
                    activeProjectIds.add(project.getProjectId());
                }
            }
        }

        int activeProjects = activeProjectIds.size();

        return LeaderDashboardResponse.builder()
                .leaderRole(normalizedRole)
                .leaderName(leader.getName())
                .members(memberInfos)
                .totalMembers(totalMembers)
                .activeProjects(activeProjects)
                .totalWeight(totalWeight)
                .totalEstimatedCost(totalEstimatedCost)
                .build();
    }

    private boolean isActiveStatus(String status) {
        if (status == null) return false;
        return status.equals("ACTIVE") ||
                status.contains("EXECUTION") ||
                status.contains("DEMO") ||
                status.contains("TESTING") ||
                status.equals("PROMOTION");
    }

    private LeaderDashboardResponse.MemberProjectInfo buildMemberProjectInfo(User user, List<SysProjectMember> projectMembers, List<String> leaderProjectIds) {
        List<SysProjectMember> filteredMembers = projectMembers.stream()
                .filter(pm -> leaderProjectIds.contains(pm.getProjectId()))
                .collect(Collectors.toList());

        List<String> projectIds = filteredMembers.stream()
                .map(SysProjectMember::getProjectId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, SysProject> projectMap = projectRepository.findAllById(projectIds)
                .stream()
                .collect(Collectors.toMap(SysProject::getProjectId, p -> p));

        List<LeaderDashboardResponse.ProjectParticipation> participations = filteredMembers.stream()
                .map(pm -> {
                    SysProject project = projectMap.get(pm.getProjectId());
                    if (project == null) return null;

                    String managerName = Optional.ofNullable(project.getManager())
                            .map(User::getName)
                            .orElse("未知");

                    return LeaderDashboardResponse.ProjectParticipation.builder()
                            .projectId(pm.getProjectId())
                            .projectName(project.getName())
                            .projectType(project.getProjectType()!=null ? project.getProjectType().name() : "PROJECT")
                            .flowType(project.getFlowType() != null ? project.getFlowType().name() : "PROJECT")
                            .memberRole(pm.getRole())
                            .weight(pm.getWeight() != null ? pm.getWeight() : 0)
                            .status(project.getCurrentStatus())
                            .managerName(managerName)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int totalWeight = participations.stream()
                .mapToInt(LeaderDashboardResponse.ProjectParticipation::getWeight)
                .sum();

        double estimatedCost = totalWeight * 1000.0;

        return LeaderDashboardResponse.MemberProjectInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .avatar(user.getAvatar())
                .projects(participations)
                .totalWeight(totalWeight)
                .estimatedCost(estimatedCost)
                .build();
    }

    @Transactional
    public void assignRoleToUser(String userId, String role, Boolean isLeader) {
        String normalizedRole = role.toUpperCase();

        Optional<UserRole> existing = userRoleRepository.findByUserIdAndRole(userId, normalizedRole);

        if (existing.isPresent()) {
            UserRole userRole = existing.get();
            userRole.setIsLeader(isLeader != null ? isLeader : false);
            userRoleRepository.save(userRole);
        } else {
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .role(normalizedRole)
                    .isLeader(isLeader != null ? isLeader : false)
                    .build();
            userRoleRepository.save(userRole);
        }
    }

    @Transactional
    public void removeRoleFromUser(String userId, String role) {
        userRoleRepository.findByUserIdAndRole(userId, role.toUpperCase())
                .ifPresent(userRoleRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<User> getMembersByRole(String role) {
        return userRoleRepository.findByRoleWithUser(role.toUpperCase())
                .stream()
                .map(UserRole::getUser)
                .collect(Collectors.toList());
    }
}
