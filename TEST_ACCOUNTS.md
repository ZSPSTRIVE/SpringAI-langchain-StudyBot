# 🔑 测试账号说明

## ⚠️ 当前问题

数据库中的密码hash可能不匹配。有两种解决方案：

---

## 解决方案1：注册新用户（推荐）

直接在前端注册一个新用户，密码会自动加密并保存到数据库。

### 注册步骤

1. 访问前端：http://localhost:5173
2. 点击"注册"
3. 填写注册信息：

**学生注册示例**:
```
用户名: teststu
密码: 123456
确认密码: 123456
真实姓名: 测试学生
角色: 学生
邮箱: teststu@test.com
学号: 2021999
专业: 计算机科学与技术
班级: 计科2101
年级: 2021
学院: 计算机学院
```

**教师注册示例**:
```
用户名: testteacher
密码: 123456
确认密码: 123456
真实姓名: 测试教师
角色: 教师
邮箱: testteacher@test.com
工号: T2021999
职称: 讲师
学院: 计算机学院
研究方向: 软件工程
办公室: A楼201
```

4. 注册成功后使用新账号登录

---

## 解决方案2：修复数据库密码

如果必须使用预置账号，请执行以下SQL：

### 方法A：使用MySQL命令行

```bash
mysql -u root -p
```

然后执行：

```sql
USE qa_system_v2;

-- 删除所有旧用户
DELETE FROM user WHERE username IN ('admin', 'student1', 'teacher1');

-- 重新插入用户（使用正确的BCrypt密码）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `email`, `gender`, `status`) VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员', 'ADMIN', 'admin@qa.com', 'U', 'ACTIVE'),
('student1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '张三', 'STUDENT', 'zhangsan@student.com', 'M', 'ACTIVE'),
('teacher1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '李教授', 'TEACHER', 'lijiaoshou@teacher.com', 'M', 'ACTIVE');

-- 验证
SELECT id, username, role, status FROM user WHERE username IN ('admin', 'student1', 'teacher1');
```

### 方法B：通过Postman/curl测试

使用Postman或curl直接调用注册API：

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "realName": "测试用户",
    "role": "STUDENT",
    "email": "testuser@test.com",
    "studentNo": "2021888",
    "major": "计算机科学",
    "className": "计科2101",
    "grade": 2021,
    "college": "计算机学院"
  }'
```

---

## 🔍 调试步骤

如果还是不行，请：

1. **查看后端控制台日志**
   - 找到最新的错误信息
   - 特别是`AuthServiceImpl.login`方法的日志

2. **检查数据库**
   ```sql
   SELECT id, username, password, role, status 
   FROM user 
   WHERE username = 'admin';
   ```
   
   密码字段应该是以`$2a$10$`开头的60字符BCrypt hash

3. **验证BCrypt**
   - 原始密码: `123456`
   - BCrypt hash应该是: `$2a$10$...`（60个字符）
   - 如果密码字段是其他格式，说明加密方式不对

---

## ✅ 成功标志

登录成功后，应该看到：

**后端日志**:
```
用户登录成功: userId=1, username=admin
```

**前端界面**:
- 跳转到对应的仪表板
- 显示用户名和头像
- 可以正常浏览页面

---

## 📝 密码加密说明

本系统使用**BCrypt**加密算法，特点：
- **强度**: 10轮加密
- **盐值**: 自动生成，每次加密结果不同
- **验证**: 使用`BCryptPasswordEncoder.matches()`方法
- **格式**: `$2a$10$` + 22字符盐 + 31字符hash = 60字符

示例：
```
原始密码: 123456
加密结果: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
```

**注意**: 每次加密同一个密码，结果都不一样，这是正常的！

---

## 🎯 推荐做法

**最简单的方法**: 使用前端注册功能创建新用户，这样密码绝对正确！

1. 打开 http://localhost:5173
2. 点击"注册"按钮
3. 填写信息并提交
4. 使用新账号登录

这样就可以确保密码是正确加密的了！

