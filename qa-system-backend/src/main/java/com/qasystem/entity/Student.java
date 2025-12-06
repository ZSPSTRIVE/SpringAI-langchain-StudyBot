package com.qasystem.entity;

// MyBatis-Plus注解 - 用于映射数据库表和处理通用字段
import com.baomidou.mybatisplus.annotation.*;
// Lombok注解 - 自动生成getter、setter等方法
import lombok.Data;

// 序列化接口 - 允许对象转换为字节流,可存入缓存或网络传输
import java.io.Serializable;
// Java 8 日期时间API
import java.time.LocalDateTime;

/**
 * 学生信息实体类 - 对应数据库student表
 * 
 * 🎯 作用说明：
 * 这个类存储学生的学业相关信息,是User表的扩展
 * 就像学校档案室里每个学生都有一份学籍档案,记录专业、班级等信息
 * 
 * 📊 对应数据库表: student
 * 
 * 🔗 关系说明：
 * - 与User表是一对一关系(通过userId关联)
 * - 一个用户(User)如果角色是STUDENT,就会有一条Student记录
 * - User表存储基本信息(用户名、密码、邮箱等)
 * - Student表存储学生特有信息(学号、专业、班级等)
 * 
 * 💡 设计思路(为什么要分两张表?):
 * 1. 不同角色有不同的信息: 学生有学号,教师有工号
 * 2. 保持User表简洁: 只存储通用字段
 * 3. 扩展性好: 以后添加其他角色(如管理员)不影响User表
 * 
 * 使用场景：
 * 1. 学生注册时: 创建User记录后,同时创建Student记录
 * 2. 查看学生详情: JOIN User和Student表获取完整信息
 * 3. 按专业筛选: 在Student表中查询特定专业的学生
 * 
 * 📝 注解说明：
 * @Data - Lombok注解,自动生成getter、setter、toString、equals、hashCode方法
 * @TableName - 指定对应的数据库表名
 * implements Serializable - 实现序列化,可以存入Redis缓存
 * 
 * @author QA System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data  // 自动生成所有字段的getter和setter方法,不用手写
@TableName("student")  // 告诉MyBatis-Plus这个类对应数据库的student表
public class Student implements Serializable {

    /**
     * 主键ID - 学生记录的唯一标识
     * 
     * 技术说明:
     * @TableId - MyBatis-Plus的主键注解
     * type = IdType.AUTO - 使用数据库自增策略
     * 
     * 数据库配置:
     * - MySQL中设置为 AUTO_INCREMENT
     * - 插入记录时不需要手动设置,数据库会自动分配
     * 
     * 例如: 第一个学生id=1, 第二个id=2, 依此类推
     * 
     * 注意: 这个ID和学号(studentNo)不是同一个东西
     * - id: 数据库内部的唯一标识,用户看不到
     * - studentNo: 学校分配的学号,用户可见(如2024001)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID - 指向User表的外键
     * 
     * 关联关系:
     * - 这个字段连接Student表和User表
     * - 通过userId可以查到这个学生的基本信息(用户名、邮箱等)
     * 
     * 数据流程:
     * 1. 用户注册 -> 先在User表创建记录,得到userId
     * 2. 继续填写学生信息 -> 在Student表创建记录,存入userId
     * 3. 查询学生完整信息 -> JOIN两张表: SELECT * FROM user u JOIN student s ON u.id = s.user_id
     * 
     * 约束:
     * - 不能为空(每个学生必须对应一个用户)
     * - 应该是唯一的(一个用户只能对应一个学生记录)
     * - 外键关联User表的id字段
     * 
     * 示例: 如果User表中张三的id=10,那么Student表中张三的userId=10
     */
    private Long userId;

    /**
     * 学号 - 学校分配给学生的唯一编号
     * 
     * 业务含义:
     * - 学号是学生在学校的官方标识
     * - 通常包含年份、学院、专业等信息的编码
     * - 就像身份证号,终身不变
     * 
     * 格式示例:
     * - "2024001" - 2024年入学,编号001
     * - "CS20240001" - 计算机学院(CS)2024年入学,编号0001
     * - "202401010001" - 2024年01月01日入学,编号0001
     * 
     * 数据约束:
     * - 应该唯一(unique索引)
     * - 可以为空(注册时可能还没分配学号)
     * - 长度一般8-20个字符
     * 
     * 用途:
     * - 登录系统(可以用学号代替用户名)
     * - 成绩查询
     * - 学籍管理
     */
    private String studentNo;

    /**
     * 专业 - 学生所学的专业名称
     * 
     * 业务含义:
     * - 学生所在的学科领域
     * - 决定了课程设置和培养方向
     * 
     * 示例:
     * - "计算机科学与技术"
     * - "软件工程"
     * - "信息安全"
     * - "数据科学与大数据技术"
     * 
     * 使用场景:
     * - 按专业查询学生列表
     * - 专业相关的问题推送
     * - 统计各专业学生人数
     * 
     * 数据要求:
     * - 长度限制: 一般不超过100个字符
     * - 可以为空(可能还未确定专业)
     */
    private String major;

    /**
     * 班级 - 学生所在的行政班级
     * 
     * 业务含义:
     * - 学生日常管理的基本单位
     * - 一个班级通常30-50人
     * - 有固定的班主任和班委
     * 
     * 命名示例:
     * - "计科1班"
     * - "软件2024-1班"
     * - "CS01班"
     * 
     * 使用场景:
     * - 按班级查询学生
     * - 班级通知发送
     * - 班级作业布置
     * 
     * 数据要求:
     * - 长度限制: 一般50个字符以内
     * - 可以为空
     */
    private String className;

    /**
     * 年级 - 学生入学的年份
     * 
     * 业务含义:
     * - 标识学生是哪一届
     * - 用于区分不同入学年份的学生
     * 
     * 示例:
     * - 2024 - 2024年入学(大一)
     * - 2023 - 2023年入学(大二)
     * - 2022 - 2022年入学(大三)
     * - 2021 - 2021年入学(大四)
     * 
     * 使用场景:
     * - 按年级筛选学生
     * - 判断学生当前是几年级
     * - 毕业生信息统计
     * 
     * 计算当前年级:
     * 当前年级 = 当前年份 - grade + 1
     * 例如: 2024年,grade=2021,则为大四(2024-2021+1=4)
     * 
     * 数据类型: Integer(整数)
     * 可以为空: 是
     */
    private Integer grade;

    /**
     * 学院 - 学生所属的学院/系
     * 
     * 业务含义:
     * - 大学的二级管理单位
     * - 一个学院包含多个相关专业
     * - 比专业更大的组织单位
     * 
     * 示例:
     * - "计算机科学与技术学院"
     * - "信息工程学院"
     * - "软件学院"
     * - "人工智能学院"
     * 
     * 组织层级:
     * 学校 > 学院 > 专业 > 班级 > 学生
     * 
     * 使用场景:
     * - 按学院统计学生
     * - 学院级别的通知
     * - 跨专业但同学院的活动
     * 
     * 数据要求:
     * - 长度: 一般不超过100个字符
     * - 可以为空
     */
    private String college;

    /**
     * 创建时间 - 学生信息首次录入系统的时间
     * 
     * 技术说明:
     * @TableField(fill = FieldFill.INSERT)
     * - 插入记录时自动填充当前时间
     * - 不需要在代码中手动设置
     * - MyBatis-Plus的MetaObjectHandler会自动处理
     * 
     * 业务意义:
     * - 记录学生何时注册
     * - 用于数据审计
     * - 可以统计每天/每月的新增学生数
     * 
     * 数据类型: LocalDateTime
     * - 精确到秒: 2024-01-15 14:30:25
     * - Java 8 新的日期时间API,比Date更好用
     * 
     * 注意: 一旦创建就不会再改变(除非手动修改数据库)
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间 - 学生信息最后一次修改的时间
     * 
     * 技术说明:
     * @TableField(fill = FieldFill.INSERT_UPDATE)
     * - 插入时自动填充(等于createTime)
     * - 更新时自动填充当前时间
     * - 自动追踪最后修改时间
     * 
     * 业务意义:
     * - 追踪学生信息的变更
     * - 判断信息是否是最新的
     * - 数据同步时判断是否需要更新
     * 
     * 更新场景:
     * - 学生修改专业
     * - 更换班级
     * - 任何字段的修改都会更新这个时间
     * 
     * 示例:
     * createTime: 2024-01-15 14:30:25  (注册时间)
     * updateTime: 2024-03-20 10:15:30  (最后修改时间)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识 - 标记学生信息是否被删除
     * 
     * 技术说明:
     * @TableLogic - MyBatis-Plus的逻辑删除注解
     * - 0: 未删除(正常状态)
     * - 1: 已删除
     * 
     * 逻辑删除 vs 物理删除:
     * - 物理删除: DELETE FROM student WHERE id=1  (数据真的没了)
     * - 逻辑删除: UPDATE student SET deleted=1 WHERE id=1  (数据还在,只是标记)
     * 
     * 为什么用逻辑删除?
     * 1. 数据安全: 误删除可以恢复
     * 2. 数据审计: 保留历史记录
     * 3. 关联完整: 避免外键约束问题(如果学生发过问题,物理删除会导致问题找不到提问者)
     * 4. 合规要求: 某些场景下法律要求保留数据
     * 
     * MyBatis-Plus自动处理:
     * - 查询时: SELECT * FROM student WHERE deleted=0  (自动过滤已删除)
     * - 删除时: UPDATE student SET deleted=1 WHERE id=?  (自动改为更新)
     * 
     * 使用场景:
     * - 学生退学但保留记录
     * - 数据清理但需要保留备份
     * - 账号注销但保留历史数据
     * 
     * 注意: 如果真的需要删除数据,需要直接操作SQL,绕过MyBatis-Plus
     */
    @TableLogic
    private Integer deleted;
}

