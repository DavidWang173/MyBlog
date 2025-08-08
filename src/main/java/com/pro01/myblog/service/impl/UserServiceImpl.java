package com.pro01.myblog.service.impl;

import com.pro01.myblog.config.AvatarProperties;
import com.pro01.myblog.dto.*;
import com.pro01.myblog.mapper.UserMapper;
import com.pro01.myblog.pojo.User;
import com.pro01.myblog.service.UserService;
import com.pro01.myblog.utils.JwtUtil;
import com.pro01.myblog.utils.Md5Util;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AvatarProperties avatarProperties;

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

        // 清除缓存
        redisTemplate.delete("user:info:" + userId);
    }

    // 查看个人信息
    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        String redisKey = "user:info:" + userId;

        // 1. 先查 Redis
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null && cached instanceof UserInfoDTO dto) {
            return dto;
        }

        // 2. Redis 没有就查数据库
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 3. 封装成 DTO
        UserInfoDTO dto = UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .registerTime(user.getRegisterTime())
                .build();

        // 4. 写入 Redis（设置30分钟过期）
        redisTemplate.opsForValue().set(redisKey, dto, 30, TimeUnit.MINUTES);

        return dto;
    }

    // 上传头像
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            // 获取原始文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            // 构造完整路径
            String realPath = new File("").getAbsolutePath() + File.separator + avatarProperties.getUploadPath() + filename;
            File dest = new File(realPath);
            dest.getParentFile().mkdirs(); // 创建目录
            file.transferTo(dest);

            // 构建头像访问地址
            String avatarUrl = avatarProperties.getAccessUrlPrefix() + filename;

            // 更新数据库
            userMapper.updateAvatar(userId, avatarUrl);
            redisTemplate.delete("user:info:" + userId);

            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("本地头像上传失败", e);
        }
    }
}