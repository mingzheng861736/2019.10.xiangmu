package com.xiaoshu.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaoshu.admin.entity.TbSysConfig;
import com.xiaoshu.admin.mapper.TbSysConfigMapper;
import com.xiaoshu.admin.service.TbSysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunzhenpeng
 * @data 2019/9/25
 * @description 系统配置表 服务
 */
@Service("TbSysConfigServiceImpl")
public class TbSysConfigServiceImpl extends ServiceImpl<TbSysConfigMapper, TbSysConfig> implements TbSysConfigService {

    @Autowired
    private TbSysConfigMapper tbSysConfigMapper;

    public String getConfigValue(String configCode){
        TbSysConfig config = tbSysConfigMapper.selectOne(new QueryWrapper<TbSysConfig>().eq("config_code",configCode));
        return config.getConfigValue();
    }
}
