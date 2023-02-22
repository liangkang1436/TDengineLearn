package com.example.tdenginedatainsert.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author xiashuo
 * @date 2023/2/15 15:16
 */

@Component
@ConfigurationProperties(prefix = "task-config")
@Data
public class TaskConfig {

    private Map<String, List<String>> tables;

    private String targetHost;
    private int insertInterval;
    private int tagColCount;
    private String dataFilePath;
    private String coordinateFilePath;


}
