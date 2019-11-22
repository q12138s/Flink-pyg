package com.qs.controller;

import com.alibaba.fastjson.JSON;
import com.qs.bean.Message;
import com.qs.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @author:qisuhai
 * @date:2019/11/20
 * @description:上报服务接口
 */
@RestController
@RequestMapping("report")
public class ReportController {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @RequestMapping(value = "send",method = RequestMethod.POST)
    public void sendData(@RequestBody String str,HttpServletResponse response) throws IOException {
        //封装接受的数据
        Message message = new Message();
        message.setCount(1);
        message.setTimestamp(System.currentTimeMillis());
        message.setStr(str);
        //bean转json字符串
        String jsonStr = JSON.toJSONString(message);
        System.out.println(jsonStr);
        //发送kafka
        kafkaTemplate.send("report-1120","report",jsonStr);
        //响应状态给用户
        PrintWriter printWriter = write(response);
        printWriter.flush();
        printWriter.close();
    }

    private PrintWriter write(HttpServletResponse response) throws IOException {
        //请求头
        response.setContentType("application/json");
        //设置编码
        response.setCharacterEncoding("UTF-8");

        PrintWriter printWriter = new PrintWriter(response.getOutputStream());
        //封装响应信息
        printWriter.write("success");
        //返回响应码
        response.setStatus(HttpStatus.OK.value());
        return printWriter;

    }

}
