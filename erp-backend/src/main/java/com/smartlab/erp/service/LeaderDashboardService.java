package com.smartlab.erp.service;

import com.smartlab.erp.dto.LeaderDashboardResponse;
import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.SysProjectMember;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.enums.AccountDomain;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderDashboardService {

    private final UserRepository userRepository;
    private final SysProjectMemberRepository projectMemberRepository;
    private final SysProjectRepository projectRepository;

    public boolean isLeader(String userId, String role) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        // 系统管理员可以访问所有队长工作台
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return true;
        }

        return role.equalsIgnoreCase(user.getRole()) && Boolean.TRUE.equals(user.getIsLeader());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentLeader(String role) {
        return userRepository.findByRoleAndIsLeaderTrueAndAccountDomain(role.toUpperCase(), AccountDomain.ERP)
                .map(leader -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", leader.getUserId());
                    result.put("username", leader.getUsername());
                    result.put("name", leader.getName());
                    result.put("email", leader.getEmail());
                    result.put("avatar", leader.getAvatar());
                    result.put("role", role.toUpperCase());
                    return result;
                })
                .orElse(new HashMap<>());
    }

    @Transactional(readOnly = true)
    public LeaderDashboardResponse getLeaderDashboard(String leaderUserId, String role) {
        String normalizedRole = role.toUpperCase();

        User currentUser = userRepository.findById(leaderUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 如果不是管理员，需要验证是否是队长
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            if (!isLeader(leaderUserId, normalizedRole)) {
                throw new RuntimeException("无权访问: 用户不是该角色的队长");
            }
        }

        User leader = userRepository.findByRoleAndIsLeaderTrueAndAccountDomain(normalizedRole, AccountDomain.ERP)
                .orElse(null);

        List<User> allRoleMembers = userRepository.findByRoleAndAccountDomain(normalizedRole, AccountDomain.ERP);

        log.info("========== 队长工作台数据查询 ==========");
        log.info("当前用户ID: {}, 角色: {}, 查看的角色: {}", leaderUserId, currentUser.getRole(), normalizedRole);
        log.info("从 sys_user 表查询到的同角色成员数: {}", allRoleMembers.size());

        if (allRoleMembers.isEmpty()) {
            return buildEmptyResponse(normalizedRole, leader != null ? leader.getName() : "未知");
        }

        List<String> memberUserIds = allRoleMembers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        List<SysProjectMember> allProjectMembers = projectMemberRepository.findByUserIdInWithUser(memberUserIds);

        log.info("项目成员关系数: {}", allProjectMembers.size());

        Map<String, List<SysProjectMember>> membersByUserId = allProjectMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getUser().getUserId()));

        Set<String> allProjectIds = allProjectMembers.stream()
                .map(SysProjectMember::getProjectId)
                .collect(Collectors.toSet());

        Map<String, SysProject> projectMap = allProjectIds.isEmpty()
                ? new HashMap<>()
                : projectRepository.findAllById(allProjectIds).stream()
                .collect(Collectors.toMap(SysProject::getProjectId, p -> p));

        List<LeaderDashboardResponse.MemberProjectInfo> memberInfos = allRoleMembers.stream()
                .map(user -> {
                    List<SysProjectMember> userProjectMembers = membersByUserId.getOrDefault(user.getUserId(), new ArrayList<>());
                    return buildMemberProjectInfo(user, userProjectMembers, projectMap);
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
                if (isActiveStatus(project.getStatus())) {
                    activeProjectIds.add(project.getProjectId());
                }
            }
        }

        log.info("KPI - 成员: {}, 进行中项目: {}, 总权重: {}, 成本: {}",
                totalMembers, activeProjectIds.size(), totalWeight, totalEstimatedCost);
        log.info("======================================");

        return LeaderDashboardResponse.builder()
                .leaderRole(normalizedRole)
                .leaderName(leader != null ? leader.getName() : "未指定")
                .members(memberInfos)
                .totalMembers(totalMembers)
                .activeProjects(activeProjectIds.size())
                .totalWeight(totalWeight)
                .totalEstimatedCost(totalEstimatedCost)
                .build();
    }

    private LeaderDashboardResponse buildEmptyResponse(String role, String leaderName) {
        return LeaderDashboardResponse.builder()
                .leaderRole(role)
                .leaderName(leaderName)
                .members(new ArrayList<>())
                .totalMembers(0)
                .activeProjects(0)
                .totalWeight(0)
                .totalEstimatedCost(0.0)
                .build();
    }

    private LeaderDashboardResponse.MemberProjectInfo buildMemberProjectInfo(
            User user,
            List<SysProjectMember> projectMembers,
            Map<String, SysProject> projectMap) {

        List<LeaderDashboardResponse.ProjectParticipation> participations = projectMembers.stream()
                .map(pm -> {
                    SysProject project = projectMap.get(pm.getProjectId());
                    if (project == null) return null;

                    String managerName = Optional.ofNullable(project.getManager())
                            .map(User::getName)
                            .orElse("未知");

                    return LeaderDashboardResponse.ProjectParticipation.builder()
                            .projectId(pm.getProjectId())
                            .projectName(project.getName())
                            .projectType(project.getProjectType() != null ? project.getProjectType().name() : "PROJECT")
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
                .isLeader(Boolean.TRUE.equals(user.getIsLeader()))
                .projects(participations)
                .totalWeight(totalWeight)
                .estimatedCost(estimatedCost)
                .projectCount(participations.size())
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

    @Transactional
    public void assignRoleToUser(String userId, String role, Boolean isLeader) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRole(role.toUpperCase());
        user.setIsLeader(isLeader != null ? isLeader : false);
        userRepository.save(user);

        log.info("更新用户 {} 的角色: {}, isLeader: {}", userId, role, isLeader);
    }

    @Transactional
    public void removeRoleFromUser(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (role.equalsIgnoreCase(user.getRole())) {
            user.setRole(null);
            user.setIsLeader(false);
            userRepository.save(user);
            log.info("移除用户 {} 的角色: {}", userId, role);
        }
    }

    @Transactional(readOnly = true)
    public List<User> getMembersByRole(String role) {
        return userRepository.findByRoleAndAccountDomain(role.toUpperCase(), AccountDomain.ERP);
    }
}
