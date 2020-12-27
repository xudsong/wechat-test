微信小程序后台调用相关方法
服务器admin本地文件同步到ng：
1. admin服务代码的配置文件中配置文件路径和ng服务器信息
   upload.fileUrl.start=/home/fileTemp/uploadFile/   #文件存在的相对路径
   balance.server=[{"ip":"","port":"端口号","account":"账号","password":"密码","filePath":"http://ng的ip/file/path/"},\\
                {"ip":"","port":"端口号","account":"账号","password":"密码","voicePath":"http://ng的ip/file/path/"}]  #文件所存储服务的IP[账号,密码]  // nginx所在服务器
例如：balance.server=[{"ip":"10.123.23.76","port":"22","account":"root","password":"pwd","voicePath":"http://10.123.23.76/file/path/"},\\
                {"ip":"10.123.23.77","port":"22","account":"root","password":"pwd","voicePath":"http://10.123.23.77/file/path/"}]

2.ng所在服务器的配置文件中加入配置信息：
  balance.server中配置了几个IP，就在IP所在的nginx中加入下面的配置
  location /file/path/ {
          alias   /home/fileTemp/uploadFile/;    //  配置文件中 的 upload.fileUrl.start
          autoindex on;
        }