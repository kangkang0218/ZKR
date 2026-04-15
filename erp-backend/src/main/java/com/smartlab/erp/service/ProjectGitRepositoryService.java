package com.smartlab.erp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlab.erp.dto.*;
import com.smartlab.erp.entity.ProjectGitRepository;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.repository.ProjectGitRepositoryRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProjectGitRepositoryService {

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("github\\.com[:/]+([^/]+)/([^/.]+)(?:\\.git)?/?$");

    private final ProjectGitRepositoryRepository projectGitRepositoryRepository;
    private final SysProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ProjectGitRepositoryResponse> list(String projectId, String userId) {
        assertProjectReadable(projectId, userId);
        return projectGitRepositoryRepository.findByProjectIdAndActiveTrueOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectGitRepositoryResponse create(String projectId, String userId, ProjectGitRepositoryCreateRequest request) {
        assertProjectReadable(projectId, userId);
        if (request == null || request.getRepositoryUrl() == null || request.getRepositoryUrl().isBlank()) {
            throw new BusinessException("仓库地址不能为空");
        }

        ProjectGitRepository entity = ProjectGitRepository.builder()
                .projectId(projectId)
                .repositoryUrl(request.getRepositoryUrl().trim())
                .accessToken(request.getAccessToken())
                .branch((request.getBranch() == null || request.getBranch().isBlank()) ? "main" : request.getBranch().trim())
                .provider((request.getProvider() == null || request.getProvider().isBlank()) ? "GITHUB" : request.getProvider().trim().toUpperCase())
                .createdByUserId(userId)
                .lastTestStatus("NOT_TESTED")
                .active(true)
                .build();

        return toResponse(projectGitRepositoryRepository.save(entity));
    }

    @Transactional
    public ProjectGitRepositoryTestResponse test(String projectId, Long repoId, String userId) {
        assertProjectReadable(projectId, userId);
        ProjectGitRepository repo = projectGitRepositoryRepository.findByIdAndProjectIdAndActiveTrue(repoId, projectId)
                .orElseThrow(() -> new BusinessException("Git仓库配置不存在"));

        ProjectGitRepositoryTestResponse result = doTest(repo);
        repo.setLastTestStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
        repo.setLastTestMessage(result.getMessage());
        repo.setLastTestedAt(Instant.now());
        projectGitRepositoryRepository.save(repo);
        return result;
    }

    @Transactional(readOnly = true)
    public List<ProjectGitCommitLogDTO> fetchCommitLogs(String projectId, Long repoId, String userId, int limit) {
        assertProjectReadable(projectId, userId);
        ProjectGitRepository repo = projectGitRepositoryRepository.findByIdAndProjectIdAndActiveTrue(repoId, projectId)
                .orElseThrow(() -> new BusinessException("Git仓库配置不存在"));
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return requestCommitLogs(repo, safeLimit);
    }

    private ProjectGitRepositoryTestResponse doTest(ProjectGitRepository repo) {
        GitHubRepoRef ref = parseGitHubRepo(repo.getRepositoryUrl());
        if (ref == null) {
            return ProjectGitRepositoryTestResponse.builder()
                    .success(false)
                    .message("暂仅支持 GitHub 仓库地址")
                    .repository(repo.getRepositoryUrl())
                    .build();
        }

        try {
            HttpResponse<String> response = requestGitHubJson(
                    "https://api.github.com/repos/" + ref.owner + "/" + ref.repo,
                    repo.getAccessToken()
            );
            if (response.statusCode() / 100 != 2) {
                return ProjectGitRepositoryTestResponse.builder()
                        .success(false)
                        .message("链接测试失败，状态码: " + response.statusCode())
                        .repository(ref.owner + "/" + ref.repo)
                        .build();
            }
            JsonNode json = objectMapper.readTree(response.body());
            return ProjectGitRepositoryTestResponse.builder()
                    .success(true)
                    .message("链接测试通过")
                    .repository(json.path("full_name").asText(ref.owner + "/" + ref.repo))
                    .defaultBranch(json.path("default_branch").asText("main"))
                    .build();
        } catch (Exception e) {
            return ProjectGitRepositoryTestResponse.builder()
                    .success(false)
                    .message("链接测试异常: " + e.getMessage())
                    .repository(ref.owner + "/" + ref.repo)
                    .build();
        }
    }

    private List<ProjectGitCommitLogDTO> requestCommitLogs(ProjectGitRepository repo, int limit) {
        GitHubRepoRef ref = parseGitHubRepo(repo.getRepositoryUrl());
        if (ref == null) {
            throw new BusinessException("暂仅支持 GitHub 仓库地址拉取日志");
        }

        try {
            String branch = repo.getBranch() == null || repo.getBranch().isBlank() ? "main" : repo.getBranch().trim();
            String url = "https://api.github.com/repos/" + ref.owner + "/" + ref.repo + "/commits?sha=" + branch + "&per_page=" + limit;
            HttpResponse<String> response = requestGitHubJson(url, repo.getAccessToken());
            if (response.statusCode() / 100 != 2) {
                throw new BusinessException("获取日志失败，状态码: " + response.statusCode());
            }

            JsonNode array = objectMapper.readTree(response.body());
            List<ProjectGitCommitLogDTO> logs = new ArrayList<>();
            if (array.isArray()) {
                for (JsonNode item : array) {
                    JsonNode commit = item.path("commit");
                    JsonNode author = commit.path("author");
                    logs.add(ProjectGitCommitLogDTO.builder()
                            .sha(item.path("sha").asText(""))
                            .authorName(firstNonBlank(item.path("author").path("login").asText(null), author.path("name").asText(null), "未知提交者"))
                            .authorEmail(firstNonBlank(author.path("email").asText(null), "-"))
                            .pushedAt(author.path("date").asText(null))
                            .message(firstLine(commit.path("message").asText("")))
                            .commitUrl(item.path("html_url").asText(""))
                            .build());
                }
            }
            return logs;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("拉取 Git 操作日志失败: " + e.getMessage());
        }
    }

    private ProjectGitRepositoryResponse toResponse(ProjectGitRepository entity) {
        String creator = userRepository.findById(entity.getCreatedByUserId())
                .map(u -> u.getName() == null || u.getName().isBlank() ? u.getUsername() : u.getName())
                .orElse(entity.getCreatedByUserId());

        return ProjectGitRepositoryResponse.builder()
                .id(entity.getId())
                .repositoryUrl(entity.getRepositoryUrl())
                .branch(entity.getBranch())
                .provider(entity.getProvider())
                .createdBy(creator)
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString())
                .lastTestStatus(entity.getLastTestStatus())
                .lastTestMessage(entity.getLastTestMessage())
                .lastTestedAt(entity.getLastTestedAt() == null ? null : entity.getLastTestedAt().toString())
                .tokenConfigured(entity.getAccessToken() != null && !entity.getAccessToken().isBlank())
                .build();
    }

    private void assertProjectReadable(String projectId, String userId) {
        projectRepository.findProjectByIdAndUser(projectId, userId)
                .orElseThrow(() -> new BusinessException("无权访问该项目或项目不存在"));
    }

    private HttpResponse<String> requestGitHubJson(String url, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token.trim());
        }
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private GitHubRepoRef parseGitHubRepo(String repositoryUrl) {
        if (repositoryUrl == null) return null;
        Matcher matcher = GITHUB_REPO_PATTERN.matcher(repositoryUrl.trim());
        if (!matcher.find()) return null;
        return new GitHubRepoRef(matcher.group(1), matcher.group(2));
    }

    private String firstLine(String text) {
        if (text == null || text.isBlank()) return "";
        int idx = text.indexOf('\n');
        return idx >= 0 ? text.substring(0, idx).trim() : text.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    private record GitHubRepoRef(String owner, String repo) {}
}
