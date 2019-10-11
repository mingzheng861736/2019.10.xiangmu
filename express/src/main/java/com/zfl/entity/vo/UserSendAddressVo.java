package com.zfl.entity.vo;

import com.zfl.entity.SupperEntity;

import javax.validation.constraints.NotBlank;

/**
 * @author sunzhenpeng
 * @data 2019/10/5
 * @description 用户地址，校验类
 */
public class UserSendAddressVo  extends SupperEntity {

    @NotBlank(message = "省、市、区不能为空！")
    private String provinceCode;

    @NotBlank(message = "省、市、区不能为空！")
    private String cityCode;

    @NotBlank(message = "省、市、区不能为空！")
    private String district;

    @NotBlank(message = "详细地址不能为空！")
    private String detailedAddress;

    @NotBlank(message = "姓名不能为空！")
    private String recipients;

    @NotBlank(message = "电话不能为空！")
    private String contactNumber;
}
