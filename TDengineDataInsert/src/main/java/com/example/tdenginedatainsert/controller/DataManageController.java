package com.example.tdenginedatainsert.controller;

import com.example.tdenginedatainsert.datagenerate.DataFileGenerateService;
import com.example.tdenginedatainsert.taosd.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiashuo
 * @date 2023/2/14 20:07
 */
@RequestMapping("/")
@RestController
public class DataManageController {

    static Logger log = LogManager.getLogger();

    @Autowired
    DataService dataService;

    @Autowired
    DataFileGenerateService dataFileGenerateService;

    @RequestMapping("/generate")
    public String generate() {
        dataFileGenerateService.generate();
        return "success";
    }
    @RequestMapping("/insert")
    public String insert() {
        log.info("开始插入数据");
        dataService.createData();
        return "success";
    }
    @RequestMapping("/test")
    public String test() {
        return dataService.test();
    }

    @RequestMapping("/addColumn")
    public int test(String name) {
        return dataService.addColumn(name);
    }

    @RequestMapping("/stop")
    public String stop() {
        dataService.stopInsert();
        return "success";
    }

    @RequestMapping("/clear")
    public String clear() {
        dataService.stopInsert();
        dataService.clearData();
        return "success";
    }


}
