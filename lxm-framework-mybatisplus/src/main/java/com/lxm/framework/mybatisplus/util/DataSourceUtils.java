package com.lxm.framework.mybatisplus.util;

import org.stone.beecp.BeeDataSource;
import org.stone.beecp.BeeDataSourceConfig;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.lxm.framework.mybatisplus.FrameworkDataSourceProperties;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2022/2/23
 * @Describe
 **/
public class DataSourceUtils {

    /**
     * 创建bee连接池
     * @param properties
     * @return
     */
    public static BeeDataSource createBeeDataSource(FrameworkDataSourceProperties properties) {
        var beeDataSourceConfig =
                new BeeDataSourceConfig(
                        properties.getDriverClassName(),
                        properties.getUrl(),
                        properties.getUsername(),
                        properties.getPassword());
        var beeDataSource = beeDataSourceConfig;
        if (properties.getName() != null && !properties.getName().isBlank())
            beeDataSource.setPoolName(properties.getName());
        beeDataSource.setInitialSize(10);
        beeDataSource.setMaxActive(100);
        beeDataSource.setMaxWait(8000);
        // 闲置扫描线程间隔时间(毫秒)
        beeDataSource.setIntervalOfClearTimeout(180000);
        // JMX监控支持开关
        beeDataSource.setRegisterMbeans(false);
        // 是否打印配置信息
        beeDataSource.setPrintConfiguration(false);
        // 是否打印运行时日志
        beeDataSource.setPrintRuntimeLogs(false);
        return new BeeDataSource(beeDataSourceConfig);
    }

    /**
     * 创建druid数据源
     *
     * @return DruidDataSource
     */
    public static DruidDataSource createDruidDataSource() {
        var druidDataSource = new DruidDataSource();
        druidDataSource.setInitialSize(10);
        druidDataSource.setMaxActive(100);
        druidDataSource.setMinIdle(10);
        druidDataSource.setMaxWait(8000);
        druidDataSource.setValidationQueryTimeout(5);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60 * 1000);
        druidDataSource.setMinEvictableIdleTimeMillis(60 * 1000 * 5);
        List<Filter> filters =
                List.of(createStatFilter(), createSlf4jLogFilter(), createWallFilter());
        druidDataSource.setProxyFilters(filters);
        return druidDataSource;
    }

    /**
     * 监控，上线注释
     *
     * @return
     */
    private static StatFilter createStatFilter() {
        var statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);
        statFilter.setSlowSqlMillis(10 * 1000);
        statFilter.setMergeSql(true);
        return statFilter;
    }

    /**
     * 日志，上线注释
     *
     * @return
     */
    private static Slf4jLogFilter createSlf4jLogFilter() {
        return new Slf4jLogFilter();
    }

    private static WallFilter createWallFilter() {
        var wallConfig = new WallConfig();
        wallConfig.setAlterTableAllow(false);
        wallConfig.setTruncateAllow(false);
        wallConfig.setDropTableAllow(false);
        // 是否允许非以上基本语句的其他语句，缺省关闭，通过这个选项就能够屏蔽DDL
        wallConfig.setMultiStatementAllow(false);
        wallConfig.setNoneBaseStatementAllow(false);
        wallConfig.setUpdateWhereNoneCheck(true);
        wallConfig.setCommentAllow(true);
        // #SELECT ... INTO OUTFILE 是否允许，这个是mysql注入攻击的常见手段，缺省是禁止的
        wallConfig.setSelectIntoOutfileAllow(false);
        wallConfig.setConditionAndAlwayFalseAllow(true);
        wallConfig.setConditionAndAlwayTrueAllow(true);

        var wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig);
        // 对被认为是攻击的SQL进行LOG.error输出
        wallFilter.setLogViolation(true);
        // 对被认为是攻击的SQL抛出SQLExcepton
        wallFilter.setThrowException(true);
        return wallFilter;
    }
}
