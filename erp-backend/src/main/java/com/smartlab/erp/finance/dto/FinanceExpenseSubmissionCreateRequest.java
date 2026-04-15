package com.smartlab.erp.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class FinanceExpenseSubmissionCreateRequest {

    @NotBlank(message = "费用项目不能为空")
    private String itemName;

    private String itemCategory;

    private String itemSpecification;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private String supplierName;

    @NotBlank(message = "发票号码不能为空")
    private String invoiceNumber;

    @NotBlank(message = "发生日期不能为空")
    private String occurredAt;

    @NotBlank(message = "用途说明不能为空")
    private String purpose;

    private String remarks;

    private String departureLocation;

    private String destinationLocation;

    private String travelStartAt;

    private String travelEndAt;

    @NotNull(message = "请上传发票")
    private MultipartFile invoiceFile;
}
