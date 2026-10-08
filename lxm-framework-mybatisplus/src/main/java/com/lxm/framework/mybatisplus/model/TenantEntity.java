package com.lxm.framework.mybatisplus.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Lys
 * @Date 2022/2/25
 * @Describe
 **/
@EqualsAndHashCode(callSuper = false)
@Data
public class TenantEntity extends PlainEntity<TenantEntity> {

    private static final long serialVersionUID = -5735941053511831263L;

    @TableField(
            value = "tenant_id",
            fill = FieldFill.INSERT,
            updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.NEVER)
    private Integer tenantId;
}
