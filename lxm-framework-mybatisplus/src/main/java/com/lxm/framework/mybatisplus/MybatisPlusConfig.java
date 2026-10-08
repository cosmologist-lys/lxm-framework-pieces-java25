package com.lxm.framework.mybatisplus;

import org.stone.beecp.BeeDataSource;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.lxm.framework.common.utils.StringFormatUtils;
import com.lxm.framework.mybatisplus.extention.injector.CustomizedInjector;
import com.lxm.framework.mybatisplus.interceptor.ParameterInterceptor;
import com.lxm.framework.mybatisplus.interceptor.StatementInterceptor;
import com.lxm.framework.mybatisplus.util.DataSourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import com.lxm.framework.mybatisplus.FrameworkDataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

/**
 * @Author: Lys
 * @Date 2022/3/1
 * @Describe
 **/
@EnableConfigurationProperties({
    FrameworkDataSourceProperties.class,
    MybatisPlusConfigProperties.class
})
@MapperScan(
        basePackages = "${spring.mybatis.mapper-packages:com.lxm}",
        annotationClass = org.apache.ibatis.annotations.Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@org.springframework.boot.autoconfigure.AutoConfiguration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "lfp.mybatis",
        name = "enabled",
        havingValue = "true")
public class MybatisPlusConfig {

    @Bean("beeDataSource")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "beeDataSource")
    public BeeDataSource beeDataSource(FrameworkDataSourceProperties dataSourceProperties) {
        return DataSourceUtils.createBeeDataSource(dataSourceProperties);
    }

    /*@Bean("druidDataSource")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name="druidDataSource")
    public DruidDataSource druidDataSource(FrameworkDataSourceProperties dataSourceProperties) {
        var druidDataSource = DataSourceUtils.createDruidDataSource();
        String dbName = dataSourceProperties.getName();
        if (StringUtils.isBlank(dbName)) {
            dbName = StringFormatUtils.getDbName(dataSourceProperties.getUrl());
        }
        druidDataSource.setName(dbName);
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        return druidDataSource;
    }*/

    @Bean("parameterInterceptor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "parameterInterceptor")
    public ParameterInterceptor parameterInterceptor(
            org.springframework.beans.factory.ObjectProvider<
                            com.lxm.framework.common.principle.PrincipleProvider>
                    principles) {
        return new ParameterInterceptor(principles.getIfAvailable(() -> () -> null));
    }

    @Bean("statementInterceptor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "statementInterceptor")
    public StatementInterceptor statementInterceptor(
            org.springframework.beans.factory.ObjectProvider<
                            com.lxm.framework.common.principle.PrincipleProvider>
                    principles) {
        return new StatementInterceptor(principles.getIfAvailable(() -> () -> null));
    }

    @Bean("globalConfig")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "globalConfig")
    public GlobalConfig globalConfig() {
        var dbConfig = new GlobalConfig.DbConfig();
        // 主键类型  0:"数据库ID自增", 1:"用户输入ID",2:"全局唯一ID (数字类型唯一ID)", 3:"全局唯一ID UUID";
        dbConfig.setIdType(IdType.INPUT);
        dbConfig.setLogicDeleteValue("1").setLogicNotDeleteValue("0");
        var config = new GlobalConfig();
        return config.setDbConfig(dbConfig)
                .setBanner(false)
                .setSqlInjector(new CustomizedInjector());
    }

    /*@Bean("transactionManager")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name="transactionManager")
    @DependsOn("druidDataSource")
    public PlatformTransactionManager transactionManager(@Qualifier("druidDataSource") DruidDataSource druidDataSource) {
        return new DataSourceTransactionManager(druidDataSource);
    }*/

    @Bean("transactionManager")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "transactionManager")
    @DependsOn("beeDataSource")
    public PlatformTransactionManager transactionManager(
            @Qualifier("beeDataSource") javax.sql.DataSource beeDataSource) {
        return new DataSourceTransactionManager(beeDataSource);
    }

    @Bean("txInterceptor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "txInterceptor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            prefix = "lfp.mybatis.transactions",
            name = "enabled",
            havingValue = "true")
    @DependsOn("transactionManager")
    public TransactionInterceptor txInterceptor(
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        var properties = new Properties();
        properties.setProperty("*", "PROPAGATION_REQUIRED,-Exception");
        NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
        tas.setProperties(properties);
        var trans = new TransactionInterceptor();
        trans.setTransactionManager(transactionManager);
        trans.setTransactionAttributeSource(tas);
        return trans;
    }

    @Bean("txAdvisor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name = "txAdvisor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            prefix = "lfp.mybatis.transactions",
            name = "enabled",
            havingValue = "true")
    @DependsOn("txInterceptor")
    public AspectJExpressionPointcutAdvisor txAdvisor(
            @Qualifier("txInterceptor") TransactionInterceptor txInterceptor,
            MybatisPlusConfigProperties mybatisPlusConfigProperties) {
        var pointCutAdvisor = new AspectJExpressionPointcutAdvisor();
        pointCutAdvisor.setAdvice(txInterceptor);
        pointCutAdvisor.setExpression(mybatisPlusConfigProperties.getExpression());
        return pointCutAdvisor;
    }

    @Bean("sqlSessionFactory")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            name = "sqlSessionFactory")
    @DependsOn({"beeDataSource", "globalConfig", "parameterInterceptor", "statementInterceptor"})
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("beeDataSource") javax.sql.DataSource druidDataSource,
            @Qualifier("globalConfig") GlobalConfig globalConfig,
            @Qualifier("parameterInterceptor") ParameterInterceptor parameterInterceptor,
            @Qualifier("statementInterceptor") StatementInterceptor statementInterceptor,
            MybatisPlusConfigProperties mybatisPlusConfigProperties)
            throws Exception {
        var configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(mybatisPlusConfigProperties.getJdbcTypeForNull());
        configuration.setCacheEnabled(false);
        configuration.setMapUnderscoreToCamelCase(
                mybatisPlusConfigProperties.isMapUnderscoreToCamelCase());
        configuration.setCallSettersOnNulls(mybatisPlusConfigProperties.isCallSettersOnNulls());

        var sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(druidDataSource);
        sqlSessionFactory.setTypeAliasesPackage(
                mybatisPlusConfigProperties.getTypeAliasesPackage());
        var pathResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources =
                pathResolver.getResources(mybatisPlusConfigProperties.getResourcePathPattern());
        sqlSessionFactory.setMapperLocations(resources);

        sqlSessionFactory.setConfiguration(configuration);
        var plugins = new com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor();
        plugins.addInnerInterceptor(statementInterceptor);
        var pagination =
                new com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor(
                        com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        pagination.setMaxLimit(1000L);
        plugins.addInnerInterceptor(pagination);
        sqlSessionFactory.setPlugins(parameterInterceptor, plugins);
        sqlSessionFactory.setGlobalConfig(globalConfig);
        return sqlSessionFactory.getObject();
    }
}
