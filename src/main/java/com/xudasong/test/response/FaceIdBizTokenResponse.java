package com.xudasong.test.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("faceId授权码返回类")
public class FaceIdBizTokenResponse {

    @ApiModelProperty("faceId授权码 bizToken")
    private String bizToken;

}
