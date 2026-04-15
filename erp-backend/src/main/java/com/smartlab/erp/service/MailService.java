package com.smartlab.erp.service;

import com.smartlab.erp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void sendProvisioningMail(String toEmail, String loginUrl, String username, String temporaryPassword) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new BusinessException("邮件服务未配置，请先设置发信邮箱");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("国科九天账号开通通知");
        message.setText(buildProvisioningText(loginUrl, username, temporaryPassword));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException("开户邮件发送失败，请确认 QQ 邮箱 SMTP 授权码配置正确：" + ex.getMessage());
        }
    }

    private String buildProvisioningText(String loginUrl, String username, String temporaryPassword) {
        return String.join("\n",
                "您好，",
                "",
                "您的国科九天账号已由管理员创建，请使用以下信息登录：",
                "登录地址：" + loginUrl,
                "账号：" + username,
                "初始临时密码：" + temporaryPassword,
                "",
                "为保障账号安全，请在首次登录后立即修改密码。",
                "如果这不是您本人申请，请忽略此邮件并联系管理员。",
                "",
                "国科九天");
    }

    public void sendVerificationCode(String toEmail, String verificationCode) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new BusinessException("邮件服务未配置，请先设置发信邮箱");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("国科九天验证码");
        message.setText(buildVerificationCodeText(verificationCode));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException("验证码邮件发送失败，请确认 QQ 邮箱 SMTP 授权码配置正确：" + ex.getMessage());
        }
    }

    private String buildVerificationCodeText(String verificationCode) {
        return String.join("\n",
                "您好，",
                "",
                "您正在申请重置密码，验证码为：",
                verificationCode,
                "",
                "该验证码将在 5 分钟后失效。",
                "如果这不是您本人操作，请忽略此邮件。",
                "",
                "国科九天");
    }
}
