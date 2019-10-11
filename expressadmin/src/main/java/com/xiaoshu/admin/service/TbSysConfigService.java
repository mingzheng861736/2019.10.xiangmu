package com.xiaoshu.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoshu.admin.entity.TbSysConfig;

public interface TbSysConfigService extends IService<TbSysConfig> {
    public String getConfigValue(String configCode);
}
