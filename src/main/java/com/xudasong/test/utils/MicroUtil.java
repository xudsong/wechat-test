package com.xudasong.test.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.codehaus.xfire.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;


@Slf4j
public class MicroUtil {

    /**
     * 获取信息
     */
    public static JSONObject getUserInfo(String encryptedData, String sessionkey, String iv){
        //被加密的数据
        byte[] dataBytes = Base64.decode(encryptedData);
        //加密密钥
        byte[] keyBytes = Base64.decode(sessionkey);
        //偏移量
        byte[] ivBytes = Base64.decode(iv);
        try {
            //如果密钥不足16位，那么补足
            int base = 16;
            if(keyBytes.length % base != 0){
                int group = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[group * base];
                Arrays.fill(temp, (byte)0);
                System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
                keyBytes = temp;
            }
            //初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
            SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivBytes));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultBytes = cipher.doFinal(dataBytes);
            if (null != resultBytes && resultBytes.length > 0){
                String result = new String(resultBytes, "UTF-8");
                return JSONObject.parseObject(result);
            }
        }catch (Exception e){
            log.error("error --> {}",e);
        }
        return null;
    }

    public static String SHA1(String str){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1"); // 如果是SHA加密，只需将“SHA-1”改成“SHA”即可
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexStr = new StringBuilder();
            //字节数组转换为十六进制数
            for (int i = 0; i < messageDigest.length; i++){
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2){
                    hexStr.append(0);
                }
                hexStr.append(shaHex);
            }
            return hexStr.toString();
        }catch (NoSuchAlgorithmException e ){
            log.error("{}",e);
        }
        return null;
    }

}
