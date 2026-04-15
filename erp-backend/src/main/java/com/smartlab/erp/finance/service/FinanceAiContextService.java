package com.smartlab.erp.finance.service;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.SysProjectMember;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.entity.InternalMessage;
import com.smartlab.erp.entity.ProjectChatMessage;
import com.smartlab.erp.entity.ProjectGitRepository;
import com.smartlab.erp.enums.AccountDomain;
import com.smartlab.erp.finance.dto.FinanceAiContextBlock;
import com.smartlab.erp.repository.InternalMessageRepository;
import com.smartlab.erp.repository.ProjectChatMessageRepository;
import com.smartlab.erp.repository.ProjectGitRepositoryRepository;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceAiContextService {

    private static final List<String> APPROVED_SOURCE_TYPES = List.of(
            "erp_project_portfolio",
            "erp_project_summary",
            "erp_member_activity",
            "erp_project_chat",
            "erp_internal_message",
            "erp_git_repository",
            "erp_project_member_ranking",
            "erp_user_participation_ranking",
            "erp_project_recent_activity_ranking"
    );

    private final SysProjectRepository sysProjectRepository;
    private final SysProjectMemberRepository sysProjectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectChatMessageRepository projectChatMessageRepository;
    private final InternalMessageRepository internalMessageRepository;
    private final ProjectGitRepositoryRepository projectGitRepositoryRepository;

    @Transactional(readOnly = true)
    public List<FinanceAiContextBlock> buildContextBlocks() {
        List<SysProject> projects = sysProjectRepository.findAll();
        List<SysProjectMember> projectMembers = sysProjectMemberRepository.findAll();
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getAccountDomain() == AccountDomain.ERP)
                .toList();
        List<ProjectChatMessage> projectChatMessages = projectChatMessageRepository.findAll();
        List<InternalMessage> internalMessages = internalMessageRepository.findAll();
        List<ProjectGitRepository> gitRepositories = projectGitRepositoryRepository.findAll();

        List<FinanceAiContextBlock> blocks = new ArrayList<>();
        blocks.add(contextBlock(
                "Project Portfolio Overview",
                buildProjectPortfolioContent(projects, projectMembers),
                "erp_project_portfolio",
                "all-projects"));

        blocks.addAll(buildProjectSummaryBlocks(projects, projectMembers));
        blocks.addAll(buildMemberActivityBlocks(users, projects, projectMembers));
        blocks.addAll(buildProjectChatBlocks(projects, projectChatMessages));
        blocks.addAll(buildInternalMessageBlocks(users, internalMessages, projects));
        blocks.addAll(buildGitRepositoryBlocks(projects, gitRepositories));
        blocks.addAll(buildProjectMemberRankingBlocks(projects, projectMembers));
        blocks.addAll(buildUserParticipationRankingBlocks(users, projects, projectMembers));
        blocks.addAll(buildProjectRecentActivityBlocks(projects, projectMembers, projectChatMessages, internalMessages));

        return blocks.stream()
                .filter(block -> APPROVED_SOURCE_TYPES.contains(block.getSourceType()))
                .toList();
    }

    public List<String> approvedSourceTypes() {
        return APPROVED_SOURCE_TYPES;
    }

    private FinanceAiContextBlock contextBlock(String title, String content, String sourceType, String sourceKey) {
        return FinanceAiContextBlock.builder()
                .title(title)
                .content(content)
                .sourceType(sourceType)
                .sourceKey(sourceKey)
                .build();
    }

    private String buildProjectPortfolioContent(List<SysProject> projects, List<SysProjectMember> projectMembers) {
        if (projects == null || projects.isEmpty()) {
            return "No ERP project portfolio rows are available.";
        }

        java.util.Set<String> activeProjectIds = projects.stream()
                .map(SysProject::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long activeMembershipCount = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .map(SysProjectMember::getProjectId)
                .filter(activeProjectIds::contains)
                .count();

        long projectFlowCount = projects.stream().filter(project -> project.getFlowType() != null && "PROJECT".equals(project.getFlowType().name())).count();
        long productFlowCount = projects.stream().filter(project -> project.getFlowType() != null && "PRODUCT".equals(project.getFlowType().name())).count();
        long researchFlowCount = projects.stream().filter(project -> project.getFlowType() != null && "RESEARCH".equals(project.getFlowType().name())).count();
        long activeManagers = projects.stream()
                .map(SysProject::getManager)
                .filter(Objects::nonNull)
                .map(User::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        String statusSummary = projects.stream()
                .collect(Collectors.groupingBy(SysProject::getCurrentStatus, Collectors.counting()))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));

        return "Projects " + projects.size()
                + ", memberships " + activeMembershipCount
                + ", managers " + activeManagers
                + ", project flow " + projectFlowCount
                + ", product flow " + productFlowCount
                + ", research flow " + researchFlowCount
                + ", statuses [" + statusSummary + "]";
    }

    private List<FinanceAiContextBlock> buildProjectSummaryBlocks(List<SysProject> projects, List<SysProjectMember> projectMembers) {
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, List<SysProjectMember>> membersByProject = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .collect(Collectors.groupingBy(SysProjectMember::getProjectId));

        return projects.stream()
                .sorted(Comparator.comparing(SysProject::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(project -> {
                    List<SysProjectMember> members = membersByProject.getOrDefault(project.getProjectId(), List.of());
                    String roleSummary = members.stream()
                            .map(SysProjectMember::getRole)
                            .filter(Objects::nonNull)
                            .map(role -> role.toUpperCase(Locale.ROOT))
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet().stream()
                            .sorted(java.util.Map.Entry.comparingByKey())
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .collect(Collectors.joining(", "));
                    String managerName = project.getManager() == null || project.getManager().getAccountDomain() != AccountDomain.ERP ? "n/a" : erpUserLabel(project.getManager());
                    String memberNames = members.stream()
                            .map(SysProjectMember::getUser)
                            .filter(Objects::nonNull)
                            .filter(user -> user.getAccountDomain() == AccountDomain.ERP)
                            .map(this::erpUserLabel)
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(", "));
                    String content = "Project " + project.getName()
                            + ", flow " + (project.getFlowType() == null ? "n/a" : project.getFlowType().name())
                            + ", status " + project.getCurrentStatus()
                            + ", manager " + managerName
                            + ", members " + members.size()
                            + ", roles [" + (roleSummary.isBlank() ? "none" : roleSummary) + "]"
                            + ", participants [" + (memberNames.isBlank() ? "none" : memberNames) + "]";
                    return contextBlock(project.getName() + " Summary", content, "erp_project_summary", project.getProjectId());
                })
                .toList();
    }

    private List<FinanceAiContextBlock> buildMemberActivityBlocks(List<User> users,
                                                                  List<SysProject> projects,
                                                                  List<SysProjectMember> projectMembers) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, List<SysProject>> initiatedProjectsByUser = (projects == null ? List.<SysProject>of() : projects).stream()
                .filter(project -> project.getManager() != null && project.getManager().getUserId() != null)
                .collect(Collectors.groupingBy(project -> project.getManager().getUserId()));

        java.util.Set<String> activeProjectIds = (projects == null ? List.<SysProject>of() : projects).stream()
                .map(SysProject::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        java.util.Map<String, List<SysProjectMember>> membershipsByUser = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .filter(member -> activeProjectIds.contains(member.getProjectId()))
                .filter(member -> member.getUser() != null && member.getUser().getUserId() != null)
                .filter(member -> member.getUser().getAccountDomain() == AccountDomain.ERP)
                .collect(Collectors.groupingBy(member -> member.getUser().getUserId()));

        java.util.Map<String, SysProject> projectById = (projects == null ? List.<SysProject>of() : projects).stream()
                .collect(Collectors.toMap(SysProject::getProjectId, Function.identity(), (left, right) -> left));

        return users.stream()
                .sorted(Comparator.comparing(User::getUserId))
                .map(user -> {
                    List<SysProject> initiated = initiatedProjectsByUser.getOrDefault(user.getUserId(), List.of());
                    List<SysProjectMember> memberships = membershipsByUser.getOrDefault(user.getUserId(), List.of());
                    List<SysProjectMember> uniqueMemberships = memberships.stream()
                            .collect(Collectors.toMap(
                                    SysProjectMember::getProjectId,
                                    Function.identity(),
                                    (left, right) -> left))
                            .values().stream()
                            .sorted(Comparator.comparing(SysProjectMember::getProjectId))
                            .toList();

                    String initiatedNames = initiated.stream()
                            .map(SysProject::getName)
                            .filter(Objects::nonNull)
                            .sorted()
                            .collect(Collectors.joining(", "));

                    String participationNames = uniqueMemberships.stream()
                            .map(member -> projectById.get(member.getProjectId()))
                            .filter(Objects::nonNull)
                            .map(project -> project.getName() + "(" + memberRoleForUser(projectMembers, user.getUserId(), project.getProjectId()) + ")")
                            .sorted()
                            .collect(Collectors.joining(", "));

                    String content = "User " + erpUserLabel(user)
                            + ", username " + user.getUsername()
                            + ", domain " + (user.getAccountDomain() == null ? "n/a" : user.getAccountDomain().name())
                            + ", role " + (user.getRole() == null ? "n/a" : user.getRole())
                            + ", initiated projects " + initiated.size()
                            + " [" + (initiatedNames.isBlank() ? "none" : initiatedNames) + "]"
                            + ", participated projects " + uniqueMemberships.size()
                            + " [" + (participationNames.isBlank() ? "none" : participationNames) + "]";

                    return contextBlock(erpUserLabel(user) + " Activity", content, "erp_member_activity", user.getUserId());
                })
                .toList();
    }

    private List<FinanceAiContextBlock> buildProjectChatBlocks(List<SysProject> projects,
                                                               List<ProjectChatMessage> projectChatMessages) {
        if (projectChatMessages == null || projectChatMessages.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, String> projectNameById = (projects == null ? List.<SysProject>of() : projects).stream()
                .collect(Collectors.toMap(SysProject::getProjectId, SysProject::getName, (left, right) -> left));

        java.util.Map<String, List<ProjectChatMessage>> messagesByProject = projectChatMessages.stream()
                .collect(Collectors.groupingBy(ProjectChatMessage::getProjectId));

        return messagesByProject.entrySet().stream()
                .map(entry -> {
                    String projectName = projectNameById.getOrDefault(entry.getKey(), "历史项目 " + entry.getKey());
                    List<ProjectChatMessage> messages = entry.getValue().stream()
                            .sorted(Comparator.comparing(ProjectChatMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                            .limit(5)
                            .toList();
                    long participants = messages.stream()
                            .map(ProjectChatMessage::getSenderUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    String latestMessages = messages.stream()
                            .sorted(Comparator.comparing(ProjectChatMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(message -> message.getSenderName() + ": " + compact(message.getContent(), 80))
                            .collect(Collectors.joining(" | "));
                    return contextBlock(
                            projectName + " Chat",
                            "Project " + projectName
                                    + ", chat messages " + entry.getValue().size()
                                    + ", active senders " + participants
                                    + ", recent dialogue [" + latestMessages + "]",
                            "erp_project_chat",
                            entry.getKey());
                })
                .sorted(Comparator.comparing(FinanceAiContextBlock::getTitle))
                .toList();
    }

    private List<FinanceAiContextBlock> buildInternalMessageBlocks(List<User> users,
                                                                   List<InternalMessage> internalMessages,
                                                                   List<SysProject> projects) {
        if (internalMessages == null || internalMessages.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, String> userNameById = (users == null ? List.<User>of() : users).stream()
                .collect(Collectors.toMap(User::getUserId, this::erpUserLabel, (left, right) -> left));
        java.util.Map<String, String> projectNameById = (projects == null ? List.<SysProject>of() : projects).stream()
                .collect(Collectors.toMap(SysProject::getProjectId, SysProject::getName, (left, right) -> left));

        java.util.Set<String> erpUserIds = (users == null ? List.<User>of() : users).stream().map(User::getUserId).collect(Collectors.toSet());

        return internalMessages.stream()
                .filter(message -> erpUserIds.contains(message.getRecipientUserId()))
                .collect(Collectors.groupingBy(InternalMessage::getRecipientUserId))
                .entrySet().stream()
                .map(entry -> {
                    List<InternalMessage> messages = entry.getValue().stream()
                            .sorted(Comparator.comparing(InternalMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                            .limit(5)
                            .toList();
                    long unreadCount = entry.getValue().stream().filter(message -> !Boolean.TRUE.equals(message.getRead())).count();
                    String recentSummary = messages.stream()
                            .sorted(Comparator.comparing(InternalMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(message -> {
                                String projectName = message.getProjectId() == null ? "general" : projectNameById.getOrDefault(message.getProjectId(), message.getProjectId());
                                return message.getMessageType() + ": " + compact(message.getTitle(), 40) + "@" + projectName;
                            })
                            .collect(Collectors.joining(" | "));
                    String userName = userNameById.getOrDefault(entry.getKey(), entry.getKey());
                    return contextBlock(
                            userName + " Inbox",
                            "Recipient " + userName
                                    + ", internal messages " + entry.getValue().size()
                                    + ", unread " + unreadCount
                                    + ", recent notifications [" + recentSummary + "]",
                            "erp_internal_message",
                            entry.getKey());
                })
                .sorted(Comparator.comparing(FinanceAiContextBlock::getTitle))
                .toList();
    }

    private List<FinanceAiContextBlock> buildGitRepositoryBlocks(List<SysProject> projects,
                                                                 List<ProjectGitRepository> gitRepositories) {
        if (gitRepositories == null || gitRepositories.isEmpty() || projects == null || projects.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, String> projectNameById = projects.stream()
                .collect(Collectors.toMap(SysProject::getProjectId, SysProject::getName, (left, right) -> left));

        return gitRepositories.stream()
                .filter(repo -> Boolean.TRUE.equals(repo.getActive()))
                .filter(repo -> projectNameById.containsKey(repo.getProjectId()))
                .sorted(Comparator.comparing(ProjectGitRepository::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(repo -> contextBlock(
                        projectNameById.get(repo.getProjectId()) + " Git Repository",
                        "Project " + projectNameById.get(repo.getProjectId())
                                + ", provider " + blankToDefault(repo.getProvider(), "GITHUB")
                                + ", branch " + blankToDefault(repo.getBranch(), "default")
                                + ", repository " + compact(repo.getRepositoryUrl(), 120)
                                + ", last test status " + blankToDefault(repo.getLastTestStatus(), "UNKNOWN")
                                + ", last test message " + compact(blankToDefault(repo.getLastTestMessage(), "none"), 80),
                        "erp_git_repository",
                        String.valueOf(repo.getId())))
                .toList();
    }

    private String memberRoleForUser(List<SysProjectMember> projectMembers, String userId, String projectId) {
        if (projectMembers == null || userId == null || projectId == null) {
            return "MEMBER";
        }
        return projectMembers.stream()
                .filter(member -> projectId.equals(member.getProjectId()))
                .filter(member -> member.getUser() != null && userId.equals(member.getUser().getUserId()))
                .map(SysProjectMember::getRole)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("MEMBER");
    }

    private String safeUserName(User user) {
        if (user == null) {
            return "Unknown User";
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getUserId() == null ? "Unknown User" : user.getUserId();
    }

    private List<FinanceAiContextBlock> buildProjectMemberRankingBlocks(List<SysProject> projects,
                                                                        List<SysProjectMember> projectMembers) {
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, List<SysProjectMember>> membersByProject = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .filter(member -> member.getUser() != null && member.getUser().getAccountDomain() == AccountDomain.ERP)
                .collect(Collectors.groupingBy(SysProjectMember::getProjectId));

        String ranking = projects.stream()
                .sorted(Comparator.comparingInt((SysProject project) -> membersByProject.getOrDefault(project.getProjectId(), List.of()).size()).reversed()
                        .thenComparing(project -> blankToDefault(project.getName(), project.getProjectId())))
                .map(project -> {
                    List<SysProjectMember> members = membersByProject.getOrDefault(project.getProjectId(), List.of());
                    String memberNames = members.stream()
                            .map(SysProjectMember::getUser)
                            .filter(Objects::nonNull)
                            .map(this::erpUserLabel)
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(", "));
                    return blankToDefault(project.getName(), project.getProjectId()) + " => participants " + members.size()
                            + " [" + (memberNames.isBlank() ? "none" : memberNames) + "]";
                })
                .collect(Collectors.joining(" | "));

        return List.of(contextBlock(
                "Project Member Ranking",
                ranking,
                "erp_project_member_ranking",
                "project-member-ranking"));
    }

    private List<FinanceAiContextBlock> buildUserParticipationRankingBlocks(List<User> users,
                                                                             List<SysProject> projects,
                                                                             List<SysProjectMember> projectMembers) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> activeProjectIds = (projects == null ? List.<SysProject>of() : projects).stream()
                .map(SysProject::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        java.util.Map<String, Long> countsByUser = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .filter(member -> activeProjectIds.contains(member.getProjectId()))
                .filter(member -> member.getUser() != null && member.getUser().getAccountDomain() == AccountDomain.ERP)
                .collect(Collectors.groupingBy(member -> member.getUser().getUserId(), Collectors.mapping(SysProjectMember::getProjectId, Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size()))));

        String ranking = users.stream()
                .sorted(Comparator.comparingLong((User user) -> countsByUser.getOrDefault(user.getUserId(), 0L)).reversed()
                        .thenComparing(this::erpUserLabel))
                .map(user -> erpUserLabel(user) + " => participated projects " + countsByUser.getOrDefault(user.getUserId(), 0L))
                .collect(Collectors.joining(" | "));

        return List.of(contextBlock(
                "User Participation Ranking",
                ranking,
                "erp_user_participation_ranking",
                "user-participation-ranking"));
    }

    private List<FinanceAiContextBlock> buildProjectRecentActivityBlocks(List<SysProject> projects,
                                                                          List<SysProjectMember> projectMembers,
                                                                          List<ProjectChatMessage> projectChatMessages,
                                                                          List<InternalMessage> internalMessages) {
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, Long> memberCountByProject = (projectMembers == null ? List.<SysProjectMember>of() : projectMembers).stream()
                .filter(member -> member.getUser() != null && member.getUser().getAccountDomain() == AccountDomain.ERP)
                .collect(Collectors.groupingBy(SysProjectMember::getProjectId, Collectors.mapping(SysProjectMember::getUser, Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size()))));
        java.util.Map<String, Long> chatCountByProject = (projectChatMessages == null ? List.<ProjectChatMessage>of() : projectChatMessages).stream()
                .collect(Collectors.groupingBy(ProjectChatMessage::getProjectId, Collectors.counting()));
        java.util.Map<String, Long> notificationCountByProject = (internalMessages == null ? List.<InternalMessage>of() : internalMessages).stream()
                .filter(message -> message.getProjectId() != null)
                .collect(Collectors.groupingBy(InternalMessage::getProjectId, Collectors.counting()));

        String ranking = projects.stream()
                .sorted(Comparator.comparing(SysProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(project -> blankToDefault(project.getName(), project.getProjectId())
                        + " => updatedAt " + formatInstant(project.getUpdatedAt())
                        + ", participants " + memberCountByProject.getOrDefault(project.getProjectId(), 0L)
                        + ", chats " + chatCountByProject.getOrDefault(project.getProjectId(), 0L)
                        + ", notifications " + notificationCountByProject.getOrDefault(project.getProjectId(), 0L))
                .collect(Collectors.joining(" | "));

        return List.of(contextBlock(
                "Project Recent Activity Ranking",
                ranking,
                "erp_project_recent_activity_ranking",
                "project-recent-activity-ranking"));
    }

    private String erpUserLabel(User user) {
        return safeUserName(user) + " [ERP / " + blankToDefault(user.getUsername(), "unknown") + " / " + blankToDefault(user.getUserId(), "unknown") + "]";
    }

    private String formatInstant(Instant value) {
        return value == null ? "n/a" : value.toString();
    }

    private String compact(String value, int maxLength) {
        if (value == null) {
            return "none";
        }
        String normalized = value.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return "none";
        }
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }


}
