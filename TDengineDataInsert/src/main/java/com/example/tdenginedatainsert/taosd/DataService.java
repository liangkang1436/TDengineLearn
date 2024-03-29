package com.example.tdenginedatainsert.taosd;

import com.example.tdenginedatainsert.config.TaskConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author xiashuo
 * @date 2023/2/14 20:05
 */
@Service
public class DataService {

    static Logger log = LogManager.getLogger();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskConfig taskConfig;

    Map<String, List<File>> allDataFile = new HashMap<>();
    Map<String, List<Thread>> allTaskThread = new HashMap<>();


    public void createData() {
        if (allTaskThread.isEmpty()) {
            init();
            for (String tableName : allDataFile.keySet()) {
                readFile2Database(tableName, allDataFile.get(tableName));
            }
        }
    }

    private void readFile2Database(String table, List<File> datafiles) {
        for (File datafile : datafiles) {
            // 开启线程读取数据，传入配置，传入文件
            log.info("开始处理 " + datafile.getName() + " 中的数据");
            Thread thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    // 考虑中断
                    try {

                        Path dataFilePath = Paths.get(datafile.getCanonicalPath());
                        Stream<String> lines = Files.lines(dataFilePath);
                        while (true) {
                            lines.forEachOrdered((line) -> {
                                if (!StringUtils.isEmpty(line)) {
                                    // 1.先读取列信息
                                    // 插入的时候不需要读取列信息
                                    // 2.按需读取本次需要写入的数据，然后写入
                                    List<String> colData = Arrays.asList(line.split(","));
                                    List<String> tagList = colData.subList(colData.size() - taskConfig.getTagColCount(), colData.size());
                                    List<String> dataList = colData.subList(1, colData.size() - taskConfig.getTagColCount());
                                    // 指定列，插入更精确，突然加列也不会报错
                                    // 列信息拆分为tag列和数据列
                                    List<String> colList = taskConfig.getTables().get(table);
                                    List<String> tagColList = colList.subList(colList.size() - taskConfig.getTagColCount(), colList.size());
                                    List<String> dataColList = colList.subList(0, colList.size() - taskConfig.getTagColCount());
                                    // 取主表的表明和子表的id来拼凑出一个子表表名
                                    String childTableName = table.toLowerCase() + "_" + tagList.get(0);
                                    // 第一列不看，自动填充，为当前时间
                                    log.info("开始处理：" + line);
                                    int update = 0;
                                    try {
                                        update = jdbcTemplate.update("INSERT INTO " + childTableName + " USING " + table + " ("+ StringUtils.join(tagColList, ",")  +")  TAGS (" + StringUtils.join(tagList, ",") + ") ("+StringUtils.join(dataColList, ",")+")  VALUES (NOW, " + StringUtils.join(dataList, ",") + ");");
                                    } catch (DataAccessException e) {
                                        // 不报错
                                        // e.printStackTrace();
                                        // throw new RuntimeException(e);
                                    }
                                    if (update == 1) {
                                        log.info(table + " 成功写入一条消息" + line);
                                    } else {
                                        log.info(table + " 表的 " + line + " 写入失败！！！");
                                    }
                                    // 暂停几秒钟
                                    try {
                                        Thread.sleep(taskConfig.getInsertInterval() * 1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                            });
                            // 3.到末尾了则需要回到文件的开头，从头开始读取
                            // 文件读取完毕，再次从头开始读取
                            lines = Files.lines(dataFilePath);
                        }
                    } catch (IOException e) {
                        log.info(table + " 的数据写入中断");
                    }
                }
            });
            List<Thread> threads = allTaskThread.getOrDefault(table, new ArrayList<>());
            threads.add(thread);
            allTaskThread.put(table, threads);
            thread.start();
        }
    }

    public void stopInsert() {
        for (String tableName : allTaskThread.keySet()) {
            for (Thread thread : allTaskThread.get(tableName)) {
                thread.interrupt();
                thread.stop();
            }
        }
        allTaskThread = new HashMap<>();
    }

    public void clearData() {
        for (String table : taskConfig.getTables().keySet()) {
            String sql = "delete from " + table;
            jdbcTemplate.update(sql);
        }
    }

    public String test() {
        return jdbcTemplate.queryForObject("select now", String.class);
    }

    /**
     * 在数据库插入数据的过程中，是可以直接修改表结构的，数据库比如MySQL本身会添加MDL锁来解决这些冲突，不需要开发者来考虑这其中的冲突问题，
     * @param column
     * @return
     */
    public Integer addColumn(String column) {
        if (StringUtils.isEmpty(column)) {
            return null;
        }
        Set<String> tables = taskConfig.getTables().keySet();
        String table = tables.iterator().next();
        String sql = "ALTER STABLE " + table + " ADD COLUMN " + column + " nchar(32) ;";
        return jdbcTemplate.update(sql);
    }



    private void init() {
        Set<String> tables = taskConfig.getTables().keySet();
        if (tables == null) {
            return;
        }
        String path = System.getProperty("user.dir");
        Path dataFolder = Paths.get(path + File.separator + taskConfig.getDataFilePath());
        if (!Files.exists(dataFolder)) {
            log.info("数据文件文件夹不存在！");
            return;
        }
        for (String table : tables) {
            try {
                Stream<Path> stream = Files.list(dataFolder);
                stream.forEach((chlilPath) -> {
                    // log.info(chlilPath.getFileName());
                    // chlilPath.getFileName() 返回的是 Path，必须 toString() 返回的才是 String
                    if (chlilPath.getFileName().toString().startsWith(table)) {
                        File file = chlilPath.toFile();
                        if (file.exists()) {
                            List<File> files = allDataFile.getOrDefault(table, new ArrayList<>());
                            files.add(file);
                            allDataFile.put(table, files);
                            log.info("成功识别数据文件：" + file.getPath());

                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
