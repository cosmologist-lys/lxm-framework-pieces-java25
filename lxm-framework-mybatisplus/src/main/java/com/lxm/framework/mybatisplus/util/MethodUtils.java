package com.lxm.framework.mybatisplus.util;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.lxm.framework.mybatisplus.common.SqlConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: Lys
 * @Date 2022/2/28
 * @Describe
 **/
@Slf4j
public class MethodUtils {

    public static String sqlLogicSet(TableInfo tableInfo) {
        var fieldList = tableInfo.getFieldList();
        var set = new StringBuilder("SET ");
        int i = 0;
        for (TableFieldInfo fieldInfo : fieldList) {
            if (StringUtils.equals(SqlConstant.UPDATED_BY_ID, fieldInfo.getProperty())
                    || StringUtils.equals(SqlConstant.UPDATED_AT, fieldInfo.getProperty())
                    || StringUtils.equals(SqlConstant.DELETED_TIME, fieldInfo.getProperty())) {
                ++i;
                if (i > 1) {
                    set.append(",");
                }
                set.append(
                        String.format(
                                fieldInfo.getCondition(),
                                fieldInfo.getColumn(),
                                fieldInfo.getProperty()));
            }
            if (fieldInfo.isLogicDelete()) {
                ++i;
                if (i > 1) {
                    set.append(",");
                }
                set.append(fieldInfo.getColumn()).append("=");
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isCharSequence(
                        fieldInfo.getPropertyType())) {
                    set.append("'").append(fieldInfo.getLogicDeleteValue()).append("'");
                } else {
                    set.append(fieldInfo.getLogicDeleteValue());
                }
            }
        }
        return set.toString();
    }
}
