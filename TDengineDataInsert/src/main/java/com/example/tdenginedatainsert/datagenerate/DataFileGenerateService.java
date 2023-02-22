package com.example.tdenginedatainsert.datagenerate;

import com.example.tdenginedatainsert.config.TaskConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author xiashuo
 * @date 2023/2/16 9:57
 */
@Service
public class DataFileGenerateService {

    static Logger log = LogManager.getLogger();


    @Autowired
    private TaskConfig taskConfig;

    Map<String, List<File>> allCoorFile = new HashMap<>();

    Map<String, BaseStream> columnAnalogyStreamMap = new HashMap<>();


    private void initColumnAnalogyStreamMap() {
        // 经度维度从数据中来
        // 高度，随机初始值,由前一个数值+-一个随机数
        // 所有字段
        // [ mbjd,mbwd,mbgd, mbsd, jd,  wd, gd, sd,
        // float
        // syyl   dqxc hx,mbhx,
        // int
        // dysyl gjddsl
        // 状态 int
        // mblx txzhllzt, sfzx, rwzxzt, ryzt, sbzt kmzzt
        // (字符串)
        // rysfz,fsjc dyzdxx

        //         id ，sbbh(字符串), name(字符串), dept army
        //         time,

        // 清空
        columnAnalogyStreamMap.clear();
        DoubleStream heightStream = DoubleStream.iterate(new Random().nextFloat() * 10000, (data) -> {
            return data + randomNegative() * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("gd", heightStream);
        DoubleStream sdStream = DoubleStream.iterate(new Random().nextFloat() * 100, (data) -> {
            return data + randomNegative() * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("sd", sdStream);
        DoubleStream heightStreamMB = DoubleStream.iterate(new Random().nextFloat() * 10000, (data) -> {
            return data + randomNegative() * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("mbgd", heightStreamMB);
        DoubleStream sdStreamMB = DoubleStream.iterate(new Random().nextFloat() * 100, (data) -> {
            return data + randomNegative() * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("mbsd", sdStreamMB);
        DoubleStream syylStream = DoubleStream.iterate(new Random().nextFloat() * 500, (data) -> {
            return data + (-1) * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("syyl", syylStream);
        DoubleStream dqxcStream = DoubleStream.iterate(new Random().nextFloat() * 500, (data) -> {
            return data + (1) * new Random().nextFloat() * 3;
        });
        columnAnalogyStreamMap.put("dqxc", dqxcStream);
        DoubleStream hxStream = DoubleStream.iterate(new Random().nextFloat(), (data) -> {
            return data + randomNegative() * new Random().nextFloat();
        });
        columnAnalogyStreamMap.put("hx", hxStream);
        DoubleStream hxStreamMB = DoubleStream.iterate(new Random().nextFloat(), (data) -> {
            return data + randomNegative() * new Random().nextFloat();
        });
        columnAnalogyStreamMap.put("mbhx", hxStreamMB);
        IntStream dysylStream = IntStream.iterate(new Random().nextInt() * 1000, (data) -> {
            return data + (-1) * new Random().nextInt(5);
        });
        columnAnalogyStreamMap.put("dysyl", dysylStream);
        IntStream gjddslStream = IntStream.iterate(0, (data) -> {
            return data + new Random().nextInt(5);
        });
        columnAnalogyStreamMap.put("gjddsl", gjddslStream);
        IntStream mblxStream = IntStream.generate(() -> {
            return new Random().nextInt(6);
        });
        columnAnalogyStreamMap.put("mblx", mblxStream);
        IntStream txzhllztStream = IntStream.generate(() -> {
            return new Random().nextInt(2);
        });
        columnAnalogyStreamMap.put("txzhllzt", txzhllztStream);
        IntStream sfzxStream = IntStream.generate(() -> {
            return new Random().nextInt(2);
        });
        columnAnalogyStreamMap.put("sfzx", sfzxStream);
        IntStream rwzxztStream = IntStream.generate(() -> {
            return new Random().nextInt(6);
        });
        columnAnalogyStreamMap.put("rwzxzt", rwzxztStream);
        IntStream ryztStream = IntStream.generate(() -> {
            return new Random().nextInt(6);
        });
        columnAnalogyStreamMap.put("ryzt", ryztStream);
        IntStream sbztStream = IntStream.generate(() -> {
            return new Random().nextInt(6);
        });
        columnAnalogyStreamMap.put("sbzt", sbztStream);
        IntStream kmzztStream = IntStream.generate(() -> {
            return new Random().nextInt(2);
        });
        columnAnalogyStreamMap.put("kmzzt", kmzztStream);
        Stream rysfzStream = Stream.generate(() -> {
            return "'测试身份证'";
        });
        columnAnalogyStreamMap.put("rysfz", rysfzStream);
        Stream fsjcStream = Stream.generate(() -> {
            return "'测试进程信息'";
        });
        columnAnalogyStreamMap.put("fsjc", fsjcStream);
        Stream dyzdxxStream = Stream.generate(() -> {
            return "'测试订装信息'";
        });
        columnAnalogyStreamMap.put("dyzdxx", dyzdxxStream);
    }


    private static int randomNegative() {
        int i = (new Random().nextInt(10) - 5) > 0 ? 1 : -1;
        return i;
    }


    public void generate() {
        // 0. 先初始化 坐标文件
        Set<String> tables = taskConfig.getTables().keySet();
        if (tables == null) {
            return;
        }
        String path = System.getProperty("user.dir");
        Path coorDataFolder = Paths.get(path + File.separator + taskConfig.getCoordinateFilePath());
        if (!Files.exists(coorDataFolder)) {
            log.info("坐标数据文件文件夹不存在！");
            return;
        }
        for (String table : tables) {
            try {
                Stream<Path> stream = Files.list(coorDataFolder);
                stream.forEach((chlilPath) -> {
                    // log.info(chlilPath.getFileName());
                    // chlilPath.getFileName() 返回的是 Path，必须 toString() 返回的才是 String
                    if (chlilPath.getFileName().toString().startsWith(table)) {
                        File file = chlilPath.toFile();
                        if (file.exists()) {
                            List<File> files = allCoorFile.getOrDefault(table, new ArrayList<>());
                            files.add(file);
                            allCoorFile.put(table, files);
                            log.info("成功识别坐标文件：" + file.getPath());
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 1. 检查路径 没有allDataFile路径，生成路径，有的话也先清空
        Path dataFolder = Paths.get(path + File.separator + taskConfig.getDataFilePath());
        if (Files.exists(dataFolder)) {
            try {
                Files.walk(dataFolder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Files.createDirectory(dataFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 2. 初始化预设的字段对应的数据流
        // initColumnAnalogyStreamMap();

        // 3. 开始造数据
        int tableId = 0;
        for (String table : allCoorFile.keySet()) {
            tableId++;
            int iterId = 0;
            for (File item : allCoorFile.get(table)) {
                iterId++;
                // id设计为前两位作为设备种类，后面为设备ID， 101
                int itemId = tableId * 100 + iterId;
                // 3.1 创建文件
                Path path1 = Paths.get(dataFolder.toString(), table + "_" + itemId + ".csv");
                Path deviceDataFile = null;
                try {
                    deviceDataFile = Files.createFile(path1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (deviceDataFile == null) {
                    continue;
                }
                // 3.2 准备信息
                //         id ，sbbh(字符串), name(字符串), dept army
                //         time,
                String deviceType = getDeviceType(table);
                String armyType = getArmyType(table);
                String deptName = getDept(table);
                String name = deviceType +"_"+ itemId;
                // 其他列
                // 流只能用一次，所以每次都得重新初始化
                initColumnAnalogyStreamMap();
                List<String> allColumns = taskConfig.getTables().get(table);
                Map<String, Iterator> colData = new HashMap<String, Iterator>();
                for (String column : allColumns) {
                    if (columnAnalogyStreamMap.containsKey(column)) {
                        Iterator iterator = columnAnalogyStreamMap.get(column).iterator();
                        colData.put(column, iterator);
                    }
                }

                // 3.3 写入信息
                try {
                    Path coorDataFilePath = Paths.get(item.getCanonicalPath());
                    Stream<String> lines = Files.lines(coorDataFilePath);
                    Path finalDeviceDataFile = deviceDataFile;
                    lines.forEach((coorStr) -> {
                        if (StringUtils.isEmpty(coorStr)) {
                            return;
                        }
                        String[] coorArr = coorStr.split(",");
                        String jd = coorArr[0];
                        String wd = coorArr[1];
                        List<String> allCols = taskConfig.getTables().get(table);
                        StringBuffer csvData = new StringBuffer("");
                        for (String colName : allCols) {
                            if (colName.equals("time")) {
                                csvData.append("NOW,");
                            } else if (colName.equals("id")) {
                                csvData.append(itemId + ",");
                            } else if (colName.equals("sbbh")) {
                                csvData.append("'" + deviceType + "',");
                            } else if (colName.equals("name")) {
                                csvData.append("'" + name + "',");
                            } else if (colName.equals("dept")) {
                                csvData.append("'" + deptName + "',");
                            } else if (colName.equals("army")) {
                                csvData.append("'" + armyType + "',");
                            }else if (colName.equals("jd")||colName.equals("mbjd")) {
                                csvData.append(jd + ",");
                            }else if (colName.equals("wd")||colName.equals("mbwd")) {
                                csvData.append(wd + ",");
                            } else if (colData.containsKey(colName)) {
                                // 无限流，总是有值的
                                String next = colData.get(colName).next().toString();
                                // 强行去掉负数
                                if (next.startsWith("-")) {
                                    next = next.substring(1);
                                }
                                csvData.append(next + ",");
                            } else {
                                csvData.append(",");
                            }
                        }
                        StringBuffer stringBuffer = csvData.deleteCharAt(csvData.length() - 1);
                        stringBuffer.append(System.lineSeparator());
                        try {
                            Files.write(finalDeviceDataFile, stringBuffer.toString().getBytes("UTF-8"), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }

    private String getDeviceType(String tableName) {
        //     设备编号：01：TJZJC，02：TK，03：LSHJP，04：LDSHJP ，05：QZJ ，06：YJFJDD ，07：TEN ，08：SIXTEEN ，09：DFDDFSC ，10：DFDDYSC';
        if (tableName.endsWith("TJZJC".toLowerCase())) {
            return "TJZJC";
        } else if (tableName.endsWith("TK".toLowerCase())) {
            return "TK";
        } else if (tableName.endsWith("LSHJP".toLowerCase())) {
            return "LSHJP";
        } else if (tableName.endsWith("LDSHJP".toLowerCase())) {
            return "LDSHJP";
        } else if (tableName.endsWith("QZJ".toLowerCase())) {
            return "QZJ";
        } else if (tableName.endsWith("YJFJDD".toLowerCase())) {
            return "YJFJDD";
        } else if (tableName.endsWith("TEN".toLowerCase())) {
            return "TEN";
        } else if (tableName.endsWith("SIXTEEN".toLowerCase())) {
            return "SIXTEEN";
        } else if (tableName.endsWith("DFDDFSC".toLowerCase())) {
            return "DFDDFSC";
        } else if (tableName.endsWith("DFDDYSC".toLowerCase())) {
            return "DFDDYSC";
        }
        return "";
    }
    private String getArmyType(String tableName) {
        //     设备编号：01：TJZJC，02：TK，03：LSHJP，04：LDSHJP ，05：QZJ ，06：YJFJDD ，07：TEN ，08：SIXTEEN ，09：DFDDFSC ，10：DFDDYSC';
        if (tableName.endsWith("TJZJC".toLowerCase())) {
            return "陆军";
        } else if (tableName.endsWith("TK".toLowerCase())) {
            return "陆军";
        } else if (tableName.endsWith("LSHJP".toLowerCase())) {
            return "陆军";
        } else if (tableName.endsWith("LDSHJP".toLowerCase())) {
            return "陆军";
        } else if (tableName.endsWith("QZJ".toLowerCase())) {
            return "海军";
        } else if (tableName.endsWith("YJFJDD".toLowerCase())) {
            return "海军";
        } else if (tableName.endsWith("TEN".toLowerCase())) {
            return "空军";
        } else if (tableName.endsWith("SIXTEEN".toLowerCase())) {
            return "空军";
        } else if (tableName.endsWith("DFDDFSC".toLowerCase())) {
            return "网军";
        } else if (tableName.endsWith("DFDDYSC".toLowerCase())) {
            return "网军";
        }
        return "";
    }

    private String getDept(String tableName) {
        // 上海物流分部
        // 江苏物流分部
        // 浙江物流分部
        // 广东物流分部
        // 广西物流分部
        // 海南物流分部
        // 湖北物流分部
        // 湖南物流分部
        // 河南物流分部
        // 四川物流分部
        //     设备编号：01：TJZJC，02：TK，03：LSHJP，04：LDSHJP ，05：QZJ ，06：YJFJDD ，07：TEN ，08：SIXTEEN ，09：DFDDFSC ，10：DFDDYSC';
        if (tableName.endsWith("TJZJC".toLowerCase())) {
            return "上海物流分部";
        } else if (tableName.endsWith("TK".toLowerCase())) {
            return "江苏物流分部";
        } else if (tableName.endsWith("LSHJP".toLowerCase())) {
            return "浙江物流分部";
        } else if (tableName.endsWith("LDSHJP".toLowerCase())) {
            return "广东物流分部";
        } else if (tableName.endsWith("QZJ".toLowerCase())) {
            return "广西物流分部";
        } else if (tableName.endsWith("YJFJDD".toLowerCase())) {
            return "海南物流分部";
        } else if (tableName.endsWith("TEN".toLowerCase())) {
            return "湖北物流分部";
        } else if (tableName.endsWith("SIXTEEN".toLowerCase())) {
            return "湖南物流分部";
        } else if (tableName.endsWith("DFDDFSC".toLowerCase())) {
            return "河南物流分部";
        } else if (tableName.endsWith("DFDDYSC".toLowerCase())) {
            return "四川物流分部";
        }
        return "";
    }

}
