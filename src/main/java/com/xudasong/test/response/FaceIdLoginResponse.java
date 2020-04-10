package com.xudasong.test.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("faceId人脸登录返回类")
public class FaceIdLoginResponse {

    @ApiModelProperty("用户凭证，为空代表人脸核身通过，但用户不存在")
    private String token;

    @ApiModelProperty("身份证号码，人脸核身通过，但用户不存在时返回")
    private String idCard;

    @ApiModelProperty("姓名，人脸核身通过，但用户不存在时返回")
    private String name;

}
