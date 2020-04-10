package com.xudasong.test.service;

import com.xudasong.test.request.WechatLoginRequest;
import com.xudasong.test.response.FaceIdBizTokenResponse;
import com.xudasong.test.response.FaceIdLoginResponse;
import com.xudasong.test.response.MiniSessionResponse;

public interface WeChatService {

    /**
     * 根据code获取微信会话信息
     * @param code
     * @return
     */
    public MiniSessionResponse getMiniSession(String code);

    /**
     * 获取AccessToken
     * @return
     */
    public String getAccessToken();

    /**
     * 直接微信授权获取手机号等信息登录
     * @param wechatLoginRequest
     * @return
     */
    public String login(WechatLoginRequest wechatLoginRequest);

    /**
     * 微信人脸登录
     * 1.前端调用后台获取biztoken
     * 2.前端通过biztoken获取相关用户信息
     *
    /**
     * 获取腾讯云biztoken
     * @return
     */
    FaceIdBizTokenResponse getBizToken();

    /**
     * 获取腾讯云人脸核身结果
     * @param bizToken
     * @return
     */
    FaceIdLoginResponse getFaceIdResultAndLogin(String bizToken);

}
