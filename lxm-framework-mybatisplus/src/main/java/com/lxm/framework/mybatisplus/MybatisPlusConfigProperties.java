package com.lxm.framework.mybatisplus;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;

/**
 * @Author: Lys
 * @Date 2022/3/2
 * @Describe
 **/
@ConfigurationProperties(prefix = "spring.mybatis")
@Data
public class MybatisPlusConfigProperties {

    private String packageName;
    private String typeAliasesPackage = "#.*.*.entity";
    private String resourcePathPattern = "classpath*:com/lxm/*/*/persistence/mapper/*.xml";
    private String expression = "execution(* #.*.*.service.impl.*.*(..))";
    private JdbcType jdbcTypeForNull = JdbcType.NULL;

    /**
     * map下划线转驼峰
     */
    private boolean mapUnderscoreToCamelCase = true;
    /**
     * null数据set
     */
    private boolean callSettersOnNulls = true;

    @PostConstruct
    public void init(){
        if (StringUtils.isBlank(this.packageName)) {
            this.packageName = "com.lxm";
        }
        this.typeAliasesPackage = StringUtils.replace(this.typeAliasesPackage, "#", this.packageName);
        this.resourcePathPattern = StringUtils.replace(this.resourcePathPattern, "#", this.packageName);
        this.expression = StringUtils.replace(this.expression,"#",this.packageName);
    }
}
