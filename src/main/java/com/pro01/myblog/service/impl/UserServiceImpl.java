package com.pro01.myblog.service.impl;

import com.pro01.myblog.dto.*;
import com.pro01.myblog.mapper.UserMapper;
import com.pro01.myblog.pojo.User;
import com.pro01.myblog.service.UserService;
import com.pro01.myblog.utils.JwtUtil;
import com.pro01.myblog.utils.Md5Util;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 用户注册
    @Override
    public void register(UserRegisterDTO dto) {
        // 校验验证码
        String cacheKey = "captcha:" + dto.getCaptchaId();
        String correctCode = stringRedisTemplate.opsForValue().get(cacheKey);

        if (!StringUtils.hasText(correctCode) || !correctCode.equalsIgnoreCase(dto.getCaptchaCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 检查用户名是否存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 加密密码
        String encryptedPwd = Md5Util.getMD5String(dto.getPassword());

        // 创建用户
        User user = User.builder()
                .username(dto.getUsername())
                .password(encryptedPwd)
                .nickname(dto.getNickname())
                .avatar("/images/avatar/default.jpg")
                .role("USER")
                .registerTime(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insertUser(user);

        // 删除验证码
        stringRedisTemplate.delete(cacheKey);
    }

    // 用户登录
    @Override
    public UserLoginResponse login(UserLoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("用户名不存在");
        }

        // 验证密码
        if (!Md5Util.checkPassword(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 构建 claims
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "role", user.getRole()
        );

        // 生成 token
        String token = JwtUtil.genToken(claims);

        return new UserLoginResponse(token, user.getNickname(), user.getAvatar(), user.getRole());
    }

    // 修改个人信息
    @Override
    public void updateUserInfo(Long userId, UserUpdateDTO dto) {
        if (dto.getNickname() == null && dto.getSignature() == null) {
            throw new IllegalArgumentException("不能同时为空");
        }

        userMapper.updateUserFields(userId, dto.getNickname(), dto.getSignature());
    }

    // 查看个人信息
    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .registerTime(user.getRegisterTime())
                .build();
    }
}