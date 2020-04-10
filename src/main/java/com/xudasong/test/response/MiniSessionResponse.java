package com.xudasong.test.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("解密微信加密信息串实体类")
public class MiniSessionResponse {

    @ApiModelProperty("用户唯一标识")
    private String openid;

    @ApiModelProperty("会话秘钥")
    private String session_key;

    @ApiModelProperty("用户在开放平台的唯一标识符")
    private String unionid;

    @ApiModelProperty("错误码")
    private String errcode;

    @ApiModelProperty("错误信息")
    private String errmsg;

}
