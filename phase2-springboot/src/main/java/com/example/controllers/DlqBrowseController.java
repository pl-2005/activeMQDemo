package com.example.controllers;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 死信队列浏览控制器
 * 提供浏览死信队列消息的接口
 * */
@RestController
@RequestMapping("/admin/dlq")
@RequiredArgsConstructor
public class DlqBrowseController {
    private final JmsTemplate jmsTemplate;

    /**
     * 浏览死信队列消息
     * 不删除消息，仅返回消息内容
     * @return 死信队列消息列表
     */
    @GetMapping("/peek")
    public List<Map<String, Object>> peekDlq() {
        String dlq = "ActiveMQ.DLQ";
        return jmsTemplate.browse(dlq, (session, browser) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            var en = browser.getEnumeration();
            while (en.hasMoreElements()) {
                Message m = (Message) en.nextElement();
                Map<String, Object> row = new HashMap<>();
                row.put("JMSMessageID", m.getJMSMessageID());
                row.put("text", m instanceof TextMessage tm ? tm.getText() : "(non-text)");
                list.add(row);
            }
            return list;
        });
    }
}
