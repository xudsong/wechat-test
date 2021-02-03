package com.xudasong.test.request;

import lombok.Data;

@Data
public class UserInfoDto {

    private String userType;

    private PersonInfoDto personInfo;

    private String userId;

}
