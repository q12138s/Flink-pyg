package com.qs.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author:qisuhai
 * @date:2019/11/20
 * @description:
 */
@Data
@ToString
public class Message {

    private int count;
    private long timestamp;
    private String str;
}
