package com.smartlab.erp.dto;

import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.entity.ProjectType;
import lombok.Data;

import java.util.List;

@Data
public class CreateProjectRequest {

    // 1. 基本信息
    private String name;
    private String description;

    // 2. 预算
    private Double budget;

    // 3. 类型
    private ProjectType projectType;
    private FlowType flowType;

    // 🟢 修复 1: 必须加回 managerId，Service 层需要用它来指定负责人
    private String managerId;

    // 4. 成员列表
    private List<MemberReq> members;

    // 🟢 修复 2: 必须定义这个内部类，并且加上 @Data，否则 Service 无法调用 .getUserId()
    @Data
    public static class MemberReq {
        private String userId;
        private Integer weight;
    }
}