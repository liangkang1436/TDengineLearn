package com.example.tdenginedatainsert.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.taosdata.jdbc.TSDBDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Properties;

@Configuration
public class SpringConfig {

    @Autowired
    private TaskConfig taskConfig;

    @Bean(name = "dataSource", destroyMethod = "close")
    public DruidDataSource getDruidDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.taosdata.jdbc.rs.RestfulDriver");
        // jdbc:TAOS-RS://10.8.65.169:6041/server_node_base
        String targetHost = taskConfig.getTargetHost();
        druidDataSource.setUrl("jdbc:TAOS-RS://" + targetHost + ":6041/server_node_base?user=root&password=taosdata");
        Properties connProps = new Properties();
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8");
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "zh_CN.UTF-8");
        // 设置时区
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8");
        druidDataSource.setConnectProperties(connProps);
        // 总共三十个设备的数据要造
        // druidDataSource.setInitialSize(30);
        druidDataSource.setValidationQuery("select now;");
        druidDataSource.setTestWhileIdle(true);
        return druidDataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate getJdbcTemplate(DruidDataSource druidDataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(druidDataSource);
        return jdbcTemplate;
    }

}
