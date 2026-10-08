package com.lxm.framework.mybatisplus.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.spring.activerecord.Model;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.session.SqlSession;
import org.springframework.transaction.annotation.Transactional;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * @Author: Lys
 * @Date 2022/3/7
 * @Describe
 **/
@EqualsAndHashCode(callSuper = false)
@Data
public class SimpleEntity<T extends Model<?>> extends Model<T> {


    private static final long serialVersionUID = 1879206951651314542L;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT_UPDATE)
    private Boolean deleted;

    @TableField(value = "deleted_time", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime deletedTime;

}
