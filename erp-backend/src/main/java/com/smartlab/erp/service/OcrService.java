package com.smartlab.erp.service;

import com.smartlab.erp.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * [Mock] OCR 识别服务
 * 
 * 模拟调用外部 OCR 接口识别合同/账款凭证中的金额。
 * 当前为 Mock 实现，始终返回一个模拟识别金额。
 * 
 * 后续接入真实 OCR 中间件时，只需替换此类的内部实现，
 * 接口签名保持不变，对上层 Service 透明。
 */
@Slf4j
@Service
public class OcrService {

    /**
     * 模拟 OCR 凭证校验
     * 
     * @param file 上传的合同/账款凭证截图
     * @return 识别出的金额 (Mock 实现固定返回 10000.00)
     * @throws BusinessException 如果文件为空或识别"失败"
     */
    public BigDecimal verifyVoucher(MultipartFile file) {
        log.info("[OCR Mock] 开始识别凭证文件: {}", file.getOriginalFilename());

        // ===== 基础校验 =====
        if (file == null || file.isEmpty()) {
            throw new BusinessException("凭证识别失败，禁止结算：上传的文件为空");
        }

        // ===== Mock 逻辑 =====
        // 模拟 OCR 识别过程：
        // 1. 如果文件名包含 "invalid" 或 "error"，模拟识别失败
        // 2. 否则模拟识别成功，返回一个固定金额
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.contains("invalid") || fileName.contains("error"))) {
            log.warn("[OCR Mock] 凭证识别失败，文件名包含无效标记: {}", fileName);
            throw new BusinessException("凭证识别失败，禁止结算");
        }

        // 模拟识别到的金额
        BigDecimal recognizedAmount = new BigDecimal("10000.00");
        log.info("[OCR Mock] 凭证识别成功，识别金额: {}", recognizedAmount);

        return recognizedAmount;
    }

    /**
     * 模拟 OCR 金额匹配校验
     * 
     * @param file 凭证文件
     * @param expectedRevenue 项目预计收入金额 (来自项目主记录)
     * @throws BusinessException 如果金额不匹配或识别失败
     */
    public void verifyVoucherWithAmountCheck(MultipartFile file, BigDecimal expectedRevenue) {
        BigDecimal recognizedAmount = verifyVoucher(file);

        // 如果项目有预计收入，校验金额是否匹配
        // Mock 逻辑：只要识别成功就算通过（真实环境中需要精确比对）
        if (expectedRevenue != null && expectedRevenue.compareTo(BigDecimal.ZERO) > 0) {
            log.info("[OCR Mock] 金额比对 - 识别金额: {}, 预计收入: {} (Mock 模式: 直接放行)",
                    recognizedAmount, expectedRevenue);
            // TODO: 真实环境中需要对比 recognizedAmount 与 expectedRevenue
            //  如果差异超过阈值，抛出异常：
            //  throw new BusinessException("凭证金额与预计收入不匹配，禁止结算");
        }
    }
}
