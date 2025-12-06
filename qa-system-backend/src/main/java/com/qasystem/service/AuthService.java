package com.qasystem.service;

import com.qasystem.dto.LoginRequest;
import com.qasystem.dto.LoginResponse;
import com.qasystem.dto.RegisterRequest;

/**
 * AuthService - 认证服务接口
 * 
 * 🎯 作用：处理用户身份认证相关的业务逻辑
 * 就像一个“门禁系统”，负责验证用户身份、发放门禁卡（token）、注册新用户等。
 * 
 * 📝 主要功能：
 * 1. 登录：验证用户名和密码，返回登录凭证
 * 2. 注册：创建新用户账号
 * 3. 登出：销毁登录凭证
 * 4. 刷新Token：延长登录有效期
 * 
 * 💡 使用流程：
 * 1. 用户访问登录页，输入账号密码 → login()
 * 2. 新用户注册 → register()
 * 3. 用户点击退出 → logout()
 * 4. token快过期时自动刷新 → refreshToken()
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * 🎯 功能：验证用户身份并生成登录凭证
     * 就像在门禁系统刷卡，系统验证卡片后放行。
     * 
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应，包含access token、refresh token和用户信息
     * @throws 异常 如果用户名或密码错误，或者账号被封禁
     * 
     * 💬 使用场景：
     * - 用户在登录页面输入账号密码
     * - 点击“登录”按钮后调用此方法
     * - 登录成功后前端保存token，后续请求带上token
     * 
     * 🔒 安全机制：
     * - 密码在数据库中是加密存储的
     * - 连续登录失败多次会锁定账号
     * - token有效期2小时，过期后需要重新登录或刷新
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     * 
     * 🎯 功能：创建新的用户账号
     * 就像在学校办理入学手续，填写资料后获得账号。
     * 
     * @param request 注册请求，包含用户名、密码、姓名、角色等
     * @return 注册成功后自动登录，返回token和用户信息
     * @throws 异常 如果用户名已存在或数据验证失败
     * 
     * 💬 使用场景：
     * - 新用户点击“注册”进入注册页面
     * - 填写用户名、密码、姓名、邮箱等信息
     * - 选择角色（学生/教师）并填写对应的信息
     * - 点击“注册”后调用此方法
     * 
     * ⚠️ 注意：
     * - 用户名必须唯一，不能重复
     * - 学生需要填写学号、专业等信息
     * - 教师需要填写工号、职称等信息
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登出
     * 
     * 🎯 功能：销毁用户的登录凭证
     * 就像交还门禁卡，之前的卡就不能再使用了。
     * 
     * @param token 要销毁的access token
     * 
     * 💬 使用场景：
     * - 用户点击页面上的“退出登录”按钮
     * - 调用此方法后前端清空本地保存的token
     * - 跳转到登录页面
     * 
     * 🔒 安全机制：
     * - 登出后token会被加入黑名单，不能再使用
     * - 即使他人拦截到token也无法使用
     */
    void logout(String token);

    /**
     * 刷新Token
     * 
     * 🎯 功能：使用refresh token获取新的access token
     * 就像门禁卡快过期时，可以用特殊凭证换领新卡，而不需要重新登录。
     * 
     * @param refreshToken 刷新令牌，登录时返回的refresh token
     * @return 新的登录响应，包含新的access token和refresh token
     * @throws 异常 如果refresh token无效或已过期
     * 
     * 💬 使用场景：
     * - access token有效期是2小时，refresh token有效期是7天
     * - 当access token快要过期时，前端自动调用此方法
     * - 获取新的token，用户无感知，不需要重新登录
     * 
     * 🔒 安全机制：
     * - 刷新后旧的token会失效
     * - refresh token只能使用一次，刷新后会生成新的refresh token
     */
    LoginResponse refreshToken(String refreshToken);
}

