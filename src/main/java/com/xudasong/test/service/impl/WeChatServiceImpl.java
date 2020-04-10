package com.xudasong.test.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.faceid.v20180301.FaceidClient;
import com.tencentcloudapi.faceid.v20180301.models.DetectAuthRequest;
import com.tencentcloudapi.faceid.v20180301.models.DetectAuthResponse;
import com.tencentcloudapi.faceid.v20180301.models.GetDetectInfoRequest;
import com.tencentcloudapi.faceid.v20180301.models.GetDetectInfoResponse;
import com.xudasong.test.request.WechatLoginRequest;
import com.xudasong.test.response.FaceIdBizTokenResponse;
import com.xudasong.test.response.FaceIdLoginResponse;
import com.xudasong.test.response.MiniSessionResponse;
import com.xudasong.test.service.WeChatService;
import com.xudasong.test.utils.MicroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
public class WeChatServiceImpl implements WeChatService {

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    @Value("${wechat.secretId}")
    private String secretId;

    @Value("${wechat.secretKey}")
    private String secretKey;

    @Value("${wechat.ruleId}")
    private String ruleId;

    @Value("${wechat.jscode.to.sessionUrl}")
    private String jscodeToSessionUrl;

    @Value("${wechat.access.tokenUrl}")
    private String accessTokenUrl;

    //微信绑定手机号
    private static final String PURE_PHONE_NUMBER = "purePhoneNumber";
    //腾讯云人脸核身相关
    private static final String TENCENT_CLOUD_ENDPOINT = "faceid.ap-shenzhen-fsi.tencentcloudapi.com";
    private static final String TENCENT_CLOUD_REGION = "ap-shenzhen-fsi";
    private static final String TEXT = "Text";
    private static final String SUCCESS_ERRCODE = "0";
    private static final String FACEID_ERRCODE = "ErrCode";
    private static final String IDCARD = "IdCard";
    private static final String NAME = "Name";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public MiniSessionResponse getMiniSession(String code) {
        String url = String.format(jscodeToSessionUrl,appId,appSecret,code);
        log.info("code2Session 微信登录凭证,url-->>{}",url);
        String resStr = restTemplate.getForEntity(url,String.class).getBody();
        log.info("code2Session 登录小程序微信返回："+ resStr);
        MiniSessionResponse miniSession = JSONObject.parseObject(resStr, MiniSessionResponse.class);
        if (null != miniSession && !StringUtils.isEmpty(miniSession.getSession_key()) && !StringUtils.isEmpty(miniSession.getOpenid())){
            return miniSession;
        }else {
            log.error("code2Session 微信获得openId和sessionkey失败");
            return null;
        }
    }

    @Override
    public String getAccessToken() {
        String url = String.format(accessTokenUrl,appId,appSecret);
        log.info("微信请求小程序获取access_token,url-->>{}",url);
        String resStr = restTemplate.getForEntity(url,String.class).getBody();
        log.info("微信返回AccessToken："+ resStr);
        JSONObject res = JSONObject.parseObject(resStr);
        String access_token = res.getString("access_token");
        //过期时间 用于存入缓存redis
        int expires_in = res.getInteger("expires_in");
        log.info("设置保存微信access_token信息：{}，过期时间：{}",access_token,expires_in);
        return access_token;
    }

    @Override
    public String login(WechatLoginRequest wechatLoginRequest) {
        //根据code获取会话信息
        MiniSessionResponse miniSessionResponse = getMiniSession(wechatLoginRequest.getWxCode());
        JSONObject userInfo = MicroUtil.getUserInfo(wechatLoginRequest.getEncrypteData(),miniSessionResponse.getSession_key(),wechatLoginRequest.getIv());
        //获取微信绑定的手机号
        String mobile = userInfo.getString(PURE_PHONE_NUMBER);
        //生成token
        String token = UUID.randomUUID().toString();
        return token;
    }

    @Override
    public FaceIdBizTokenResponse getBizToken() {
        //腾讯云调试生成的代码
        FaceidClient client = getFaceidClient();
        String params = "{\"RuleId\":\""+ruleId+"\"}";
        DetectAuthRequest request = DetectAuthRequest.fromJsonString(params, DetectAuthRequest.class);
        try {
            DetectAuthResponse response = client.DetectAuth(request);
            log.info("请求腾讯云获取bizToken返回：{}",response);
            FaceIdBizTokenResponse faceIdBizTokenResponse = new FaceIdBizTokenResponse();
            faceIdBizTokenResponse.setBizToken(response.getBizToken());
            return faceIdBizTokenResponse;
        }catch (TencentCloudSDKException e){
            log.error("腾讯云获取bizToken失败",e);
            return null;
        }

    }

    @Override
    public FaceIdLoginResponse getFaceIdResultAndLogin(String bizToken) {
        //获取微信返回结果
        GetDetectInfoResponse response = getFaceidResult(bizToken);
        if (null == response){
            return null;
        }
        //获取校验结果
        boolean result = isFaceIdSuccess(response);
        if (!result){
            log.info("人脸核身结果不通过");
            return null;
        }
        //获取身份证，校验是否存在对应用户
        String idCard = getStringFromText(response,IDCARD);
        String name = getStringFromText(response,NAME);
        log.info("根据身份证{}查询不到用户，姓名：{}",idCard,name);
        String token = UUID.randomUUID().toString();
        FaceIdLoginResponse resp = new FaceIdLoginResponse();
        resp.setIdCard(idCard);
        resp.setName(name);
        resp.setToken(token);
        return resp;
    }

    /**
     * 生成一个腾讯云客户端
     * @return
     */
    private FaceidClient getFaceidClient(){
        Credential credential = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(TENCENT_CLOUD_ENDPOINT);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new FaceidClient(credential, TENCENT_CLOUD_REGION, clientProfile);
    }

    /**
     * 获取腾讯云人脸核身结果
     * @param bizToken
     * @return
     */
    private GetDetectInfoResponse getFaceidResult(String bizToken){
        FaceidClient client = getFaceidClient();
        String params = "{\"BizToken\":\""+bizToken+"\",\"RuleId\":\""+ruleId+"\"}";
        GetDetectInfoRequest request = GetDetectInfoRequest.fromJsonString(params, GetDetectInfoRequest.class);
        try {
            GetDetectInfoResponse response = client.GetDetectInfo(request);
            log.info("请求腾讯云获取人脸核身结果biztoken:{},返回：{}",bizToken,response);
            return response;
        }catch (TencentCloudSDKException e){
            log.error("腾讯云获取biztoken失败",e);
            return null;
        }
    }

    /**
     * 获得人脸核身校验结果
     * @param response
     * @return
     */
    private boolean isFaceIdSuccess(GetDetectInfoResponse response){
        String errCode = getStringFromText(response,FACEID_ERRCODE);
        if (StringUtils.isEmpty(errCode) && SUCCESS_ERRCODE.equals(errCode)){
            return true;
        }
        return false;
    }

    /**
     * 从GetDetectInfoResponse的Text中获取key对应的值
     * @param response
     * @param textKey
     * @return
     */
    private String getStringFromText(GetDetectInfoResponse response, String textKey){
        String detecInfo = response.getDetectInfo();
        if (!StringUtils.isEmpty(detecInfo)){
            JSONObject jsonObject = JSON.parseObject(detecInfo);
            if (null != jsonObject){
                JSONObject textJsonObj = jsonObject.getJSONObject(TEXT);
                if (null != textJsonObj){
                    String value = textJsonObj.getString(textKey);
                    log.info("从腾讯云返回结果解析出{}：{}",textKey,value);
                    return value;
                }
            }
        }
        return null;
    }

}
