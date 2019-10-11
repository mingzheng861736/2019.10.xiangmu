package com.zfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 用户地址薄
 * </p>
 *
 * @author zc
 * @since 2019-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbUserSendAddress extends SupperEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户编号
     */
    private Integer userId;

    /**
     * 省，编码
     */
    @NotBlank(message = "省、市、区不能为空！")
    private String provinceCode;

    /**
     * 市，编码
     */
    @NotBlank(message = "省、市、区不能为空！")
    private String cityCode;

    /**
     * 区，编码
     */
    @NotBlank(message = "省、市、区不能为空！")
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空！")
    private String detailedAddress;

    /**
     * 是否是默认  0为否，1为是
     */
    private String isDefault;

    /**
     * 联系人
     */
    @NotBlank(message = "姓名不能为空！")
    private String recipients;

    /**
     * 联系电话
     */
    @NotBlank(message = "电话不能为空！")
    private String contactNumber;


}
