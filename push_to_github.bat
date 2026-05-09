@echo off
chcp 65001 >nul
echo ========================================
echo 开始上传代码到 GitHub
echo ========================================
echo.

cd /d D:\develop\Corner

echo [1/5] 添加所有文件到 Git...
git add .
if errorlevel 1 (
    echo 错误：添加文件失败
    pause
    exit /b 1
)
echo 完成！
echo.

echo [2/5] 提交更改...
git commit -m "初始化项目：添加完整的后端代码结构"
if errorlevel 1 (
    echo 提示：如果没有需要提交的更改，这是正常的
)
echo.

echo [3/5] 检查远程仓库配置...
git remote -v
echo.

echo [4/5] 添加远程仓库...
git remote remove origin 2>nul
git remote add origin https://github.com/bingege-0729/Corner.git
if errorlevel 1 (
    echo 错误：添加远程仓库失败
    pause
    exit /b 1
)
echo 完成！
echo.

echo [5/5] 推送到 GitHub...
echo 注意：如果是第一次推送，可能需要输入 GitHub 用户名和密码
git push -u origin master
if errorlevel 1 (
    echo.
    echo 尝试推送到 main 分支...
    git push -u origin main
    if errorlevel 1 (
        echo 错误：推送失败
        echo 请检查：
        echo 1. 是否已登录 GitHub
        echo 2. 是否有仓库权限
        echo 3. 网络连接是否正常
        pause
        exit /b 1
    )
)
echo.

echo ========================================
echo 上传完成！
echo ========================================
pause
