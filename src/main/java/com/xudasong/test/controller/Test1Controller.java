package com.xudasong.test.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.xudasong.test.utils.ftp.BalanceServerInfo;
import com.xudasong.test.utils.ftp.FtpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *服务器admin本地文件同步到ng：
 * 1. admin服务代码的配置文件中配置文件路径和ng服务器信息
 *    upload.fileUrl.start=/home/fileTemp/uploadFile/   #文件存在的相对路径
 *    balance.server=[{"ip":"","port":"端口号","account":"账号","password":"密码","filePath":"http://ng的ip/file/path/"},\\
 *                 {"ip":"","port":"端口号","account":"账号","password":"密码","voicePath":"http://ng的ip/file/path/"}]  #文件所存储服务的IP[账号,密码]  // nginx所在服务器
 * 例如：balance.server=[{"ip":"10.123.23.76","port":"22","account":"root","password":"pwd","voicePath":"http://10.123.23.76/file/path/"},\\
 *                 {"ip":"10.123.23.77","port":"22","account":"root","password":"pwd","voicePath":"http://10.123.23.77/file/path/"}]
 *
 * 2.ng所在服务器的配置文件中加入配置信息：
 *   balance.server中配置了几个IP，就在IP所在的nginx中加入下面的配置
 *   location /file/path/ {
 *           alias   /home/fileTemp/uploadFile/;    //  配置文件中 的 upload.fileUrl.start
 *           autoindex on;
 *         }
 */

@RestController
public class Test1Controller {

    @Value("${balance.server}")
    private String serverInfo;
    @Value("${upload.fileUrl.start}")
    private StringBuffer uploadFileUrlStart;

    /**
     *
     * 文件同步到ng
     */
    public void test() {
        try {
            //获取本应用ip
            String localIp = InetAddress.getLocalHost().getHostAddress();
            //获取集群服务器的信息
            List<BalanceServerInfo> balanceServerInfoVector = getBalanceServerInfo(serverInfo);
            //文件所在路径
            String filePathName = uploadFileUrlStart + "fileName.text";
            //进行数据同步
            if (!CollectionUtils.isEmpty(balanceServerInfoVector)) {
                for (BalanceServerInfo balanceServerInfo : balanceServerInfoVector) {
                    if (balanceServerInfo.getIp().equals(localIp)) {
                        continue;
                    }
                    String destinationPath = filePathName.substring(0, filePathName.lastIndexOf("/"));
                    FtpUtils.login(balanceServerInfo, filePathName, destinationPath,"","");
                }
           }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解析负载服务器的信息
     *
     * @param serverInfo 负载服务器的信息 json字符串
     * @return 负载服务器的信息
     */
    private List<BalanceServerInfo> getBalanceServerInfo(String serverInfo) {
        List<BalanceServerInfo> balanceServerInfos = JSONArray.parseObject(serverInfo, new TypeReference<ArrayList<BalanceServerInfo>>() {
        });
        balanceServerInfos.stream().forEach(System.out::println);
        return balanceServerInfos;
    }
}