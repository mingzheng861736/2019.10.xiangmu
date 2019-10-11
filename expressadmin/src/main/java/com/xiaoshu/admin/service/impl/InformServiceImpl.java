package com.xiaoshu.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoshu.admin.entity.Inform;
import com.xiaoshu.admin.entity.TbArea;
import com.xiaoshu.admin.mapper.InformMapper;
import com.xiaoshu.admin.mapper.TbAreaMapper;
import com.xiaoshu.admin.service.InformService;
import com.xiaoshu.admin.service.TbAreaService;
import org.springframework.stereotype.Service;

@Service
public class InformServiceImpl extends ServiceImpl<InformMapper, Inform> implements InformService {
}
