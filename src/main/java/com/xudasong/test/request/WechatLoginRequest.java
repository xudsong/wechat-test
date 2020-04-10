package com.xudasong.test.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("微信登录请求类")
public class WechatLoginRequest {

    @ApiModelProperty("微信code")
    private String wxCode;

    @ApiModelProperty("加密数据")
    private String encrypteData;

    @ApiModelProperty("偏移量")
    private String iv;

}
