package com.example.tdenginedatainsert.task;

import com.example.tdenginedatainsert.datagenerate.DataFileGenerateService;
import com.example.tdenginedatainsert.taosd.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author xiashuo
 * @date 2023/3/3 14:28
 */
@Component
public class WorkWhenReady implements ApplicationRunner {

    static Logger log = LogManager.getLogger();

    @Autowired
    DataService dataService;

    @Autowired
    DataFileGenerateService dataFileGenerateService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(" ------------ system is ready------------");
        log.info(" ------------ start to generate data file ------------");
        dataFileGenerateService.generate();
        log.info(" ------------ start to generate insert file ------------");
        dataService.createData();
    }
}
