package com.pro01.myblog.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.pro01.myblog.pojo.Result;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

@RestController
public class CaptchaController {

    @Resource
    private DefaultKaptcha kaptcha;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/captcha")
    public Result<?> getCaptcha() {
        // 生成验证码文本和UUID
        String captchaText = kaptcha.createText();
        String uuid = UUID.randomUUID().toString();

        // 生成图片
        BufferedImage image = kaptcha.createImage(captchaText);

        // 转为 base64
        String base64Image;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return Result.error("验证码生成失败");
        }

        // 存入 Redis（5分钟）
        stringRedisTemplate.opsForValue().set("captcha:" + uuid, captchaText, 10, TimeUnit.MINUTES);

        System.out.println("验证码内容：" + captchaText);
        // 返回 UUID + base64
        return Result.success(new CaptchaResponse(uuid, base64Image));
    }


    // 内部类作为响应体
    record CaptchaResponse(String captchaId, String imageBase64) {}
}