-- 修复用户密码
-- 所有用户的密码都是: 123456
-- 使用BCrypt加密（strength=10）

-- 更新所有测试用户的密码为正确的BCrypt hash
UPDATE `user` SET `password` = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKfqg.aO'
WHERE username IN ('admin', 'student1', 'teacher1');

-- 验证更新
SELECT username,
       LEFT(password, 20) as password_hash,
       role,
       status
FROM user
WHERE username IN ('admin', 'student1', 'teacher1');

