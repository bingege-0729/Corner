package com.example.corner.service.impl;

import com.example.corner.common.Result;
import com.example.corner.dto.AvatarResponse;
import com.example.corner.dto.LoginRequest;
import com.example.corner.dto.LoginResponse;
import com.example.corner.entity.UserInfo;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.service.UserService;
import com.example.corner.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 文件上传目录
     */
    private static final String UPLOAD_DIR;
    
    static {
        // 获取用户目录，确保跨平台兼容
        String userDir = System.getProperty("user.dir");
        UPLOAD_DIR = userDir + File.separator + "uploads" + File.separator + "avatars" + File.separator;
    }

    /**
     * 手机号登录（自动注册）
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserInfo user = userInfoRepository.findByPhone(request.getPhone()).orElse(null);

        if (user == null) {
            user = new UserInfo();
            user.setPhone(request.getPhone());
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userInfoRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setToken(token);

        return response;
    }

    /**
     * 头像上传
     * @param userId
     * @param file
     * @return
     */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        File dest = new File(UPLOAD_DIR + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        UserInfo user = userInfoRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatarUrl("/api/user/avatar/" + fileName);
        user.setUpdatedAt(LocalDateTime.now());
        userInfoRepository.save(user);

        return user.getAvatarUrl();
    }
}
