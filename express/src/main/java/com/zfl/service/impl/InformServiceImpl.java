package com.zfl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zfl.entity.Inform;
import com.zfl.mapper.InformMapper;
import com.zfl.service.InformService;
import org.springframework.stereotype.Service;

@Service
public class InformServiceImpl extends ServiceImpl<InformMapper, Inform> implements InformService {
}
