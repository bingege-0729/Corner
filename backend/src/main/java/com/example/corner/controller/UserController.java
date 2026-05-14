package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.AvatarResponse;
import com.example.corner.dto.LoginRequest;
import com.example.corner.dto.LoginResponse;
import com.example.corner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
}
