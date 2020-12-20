package com.xudasong.test.utils.ftp;

import lombok.Data;

/**
 * 负载服务器信息
 */
@Data
public class BalanceServerInfo {

    private String ip;
    private String port;
    private String account;
    private String password;
    private String voicePath;

}
