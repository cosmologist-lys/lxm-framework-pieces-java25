package com.lxm.framework.mybatisplus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.spring.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Twenty2
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class PlainEntity<T extends Model<?>> extends SimpleEntity<T> {

    private static final long serialVersionUID = 7649725843294772985L;

    @TableField(value = "created_by_id", fill = FieldFill.INSERT)
    private Integer createdById;

    @TableField(value = "updated_by_id", fill = FieldFill.INSERT_UPDATE)
    private Integer updatedById;
}
