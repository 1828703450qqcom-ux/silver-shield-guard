@echo off
chcp 65001 >nul
echo ========================================
echo    银盾守护 - APK构建工具
echo ========================================
echo.

:: 检查Java环境
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境！
    echo 请先安装JDK 17或更高版本
    echo 下载地址：https://adoptium.net/
    pause
    exit /b 1
)
echo [✓] Java环境正常

:: 检查Android SDK
echo [2/4] 检查Android SDK...
if defined ANDROID_HOME (
    echo [✓] Android SDK: %ANDROID_HOME%
) else (
    echo [警告] 未设置ANDROID_HOME环境变量
    echo 请确保已安装Android SDK
)

:: 清理旧构建
echo [3/4] 清理旧构建文件...
if exist "app\build" (
    rd /s /q "app\build" >nul 2>&1
    echo [✓] 已清理
) else (
    echo [✓] 无需清理
)

:: 构建APK
echo [4/4] 开始构建APK...
echo.
echo ========================================
echo   正在构建，请稍候...
echo ========================================
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   [成功] APK构建完成！
    echo ========================================
    echo.
    echo APK文件位置：
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    :: 复制到桌面
    echo 正在复制到桌面...
    copy "app\build\outputs\apk\debug\app-debug.apk" "%USERPROFILE%\Desktop\银盾守护.apk" >nul 2>&1
    if %errorlevel% equ 0 (
        echo [✓] 已复制到桌面：银盾守护.apk
    ) else (
        echo [!] 复制到桌面失败，请手动复制
    )
    echo.
    
    :: 询问是否打开文件夹
    set /p OPEN_FOLDER="是否打开APK所在文件夹？(Y/N): "
    if /i "%OPEN_FOLDER%"=="Y" (
        explorer "app\build\outputs\apk\debug"
    )
) else (
    echo.
    echo ========================================
    echo   [失败] APK构建失败！
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. 未安装Android SDK
    echo 2. 网络连接问题
    echo 3. 内存不足
    echo.
    echo 请查看上方错误信息并解决问题后重试
)

echo.
pause
