package com.example.tdenginesubscribe;

import com.taosdata.jdbc.tmq.ConsumerRecords;
import com.taosdata.jdbc.tmq.MapDeserializer;
import com.taosdata.jdbc.tmq.TMQConstants;
import com.taosdata.jdbc.tmq.TaosConsumer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

/**
 * @author xiashuo
 * @date 2023/2/7 15:31
 */
@RestController
@RequestMapping("/")
public class TDengineSubscribeController {

    @RequestMapping
    public String test() {
        Properties config = new Properties();
        config.setProperty(TMQConstants.BOOTSTRAP_SERVERS,  "localhost:6030");
        // config.setProperty(TMQConstants.ENABLE_AUTO_COMMIT, "true");
        // config.setProperty(TMQConstants.AUTO_COMMIT_INTERVAL, "1000");
        // ----------------
        config.setProperty(TMQConstants.CONNECT_IP, "localhost");
        // // 定死，只能6030
        config.setProperty(TMQConstants.CONNECT_PORT, "6030");
        config.setProperty(TMQConstants.CONNECT_DB, "server_node_base");
        config.setProperty(TMQConstants.CONNECT_USER, "root");
        config.setProperty(TMQConstants.CONNECT_PASS, "taosdata");
        // config.setProperty(TMQConstants.GROUP_ID, groupId);
        config.setProperty(TMQConstants.VALUE_DESERIALIZER, MapDeserializer.class.getName());
        config.setProperty(TMQConstants.AUTO_OFFSET_RESET, "latest");
        String clientId = UUID.randomUUID().toString();
        config.setProperty(TMQConstants.CLIENT_ID, clientId);
        config.setProperty(TMQConstants.GROUP_ID, clientId);
        TaosConsumer<Map<String, Object>> taosConsumer = null;
        try {
            taosConsumer = new TaosConsumer<>(config);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        List<String> topics = Collections.singletonList("topic_1623215095759613953_dock");
        try {
            // TDEngine 主题订阅，只能通过原生链接，不能通过rest连接
            taosConsumer.subscribe(topics);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("订阅成功！");
        System.out.println(config.toString());
        try {
            poll(taosConsumer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            taosConsumer.unsubscribe();
            /* 关闭消费 */
            taosConsumer.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "succes";
    }

    private void poll(TaosConsumer<Map<String, Object>> taosConsumer) throws InterruptedException {
        while (true) {
            Thread.sleep(1000);
            ConsumerRecords<Map<String, Object>> records = null;
            try {
                records = taosConsumer.poll(Duration.ofSeconds(1));
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause() instanceof InterruptedException) {
                    // 中断了
                    break;
                } else {
                }
            } finally {
                // readLock.unlock();
            }

            if (records != null) {
                if (records.isEmpty()) {
                    continue;
                }
                for (Map<String, Object> inData : records) {
                    // message.setContent();
                    System.out.println(inData);
                }
                break;
            }
        }
    }


}
