-- 修复数据库Schema，允许学号和工号为空

USE qa_system_v2;

-- 修改学生表，允许学号为NULL
ALTER TABLE `student` 
MODIFY COLUMN `student_no` VARCHAR(50) NULL COMMENT '学号',
DROP INDEX `uk_student_no`,
ADD UNIQUE INDEX `uk_student_no` (`student_no`) USING BTREE;

-- 修改教师表，允许工号为NULL
ALTER TABLE `teacher` 
MODIFY COLUMN `teacher_no` VARCHAR(50) NULL COMMENT '工号',
DROP INDEX `uk_teacher_no`,
ADD UNIQUE INDEX `uk_teacher_no` (`teacher_no`) USING BTREE;

-- 验证修改
SHOW FULL COLUMNS FROM student WHERE Field = 'student_no';
SHOW FULL COLUMNS FROM teacher WHERE Field = 'teacher_no';

SELECT 'Schema修复完成！现在学号和工号可以为空了。' as message;

