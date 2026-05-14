package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.LoginRequest;
import com.example.corner.service.UserService;
import com.example.corner.vo.AvatarResponse;
import com.example.corner.vo.LoginResponse;
import com.example.corner.vo.UserStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    // 使用动态绝对路径
    private static final String UPLOAD_DIR;
    
    static {
        String userDir = System.getProperty("user.dir");
        UPLOAD_DIR = userDir + File.separator + "uploads" + File.separator + "avatars" + File.separator;
    }
    
    /**
     * 手机号登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 上传头像接口
     */
    @PostMapping("/avatar/upload")
    public Result<AvatarResponse> uploadAvatar(HttpServletRequest request,
                                               @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        String url = userService.uploadAvatar(userId, file);

        AvatarResponse res = new AvatarResponse();
        res.setAvatarUrl(url);
        return Result.success(res);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.logout(userId);
        return Result.success();
    }
    
    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Result<UserStatsResponse> getUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserStatsResponse stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

}
