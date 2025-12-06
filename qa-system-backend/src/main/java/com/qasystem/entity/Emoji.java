package com.qasystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表情实体类
 */
@Data
@TableName("emoji")
public class Emoji implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表情包ID */
    private Long packId;

    /** 表情名称/描述 */
    private String name;

    /** 表情图片URL */
    private String url;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 类型：STATIC-静态, GIF-动图, LOTTIE-Lottie动画 */
    private String type;

    /** 表情代码，如 [微笑] */
    private String code;

    /** 排序 */
    private Integer sortOrder;

    /** 使用次数 */
    private Integer useCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
