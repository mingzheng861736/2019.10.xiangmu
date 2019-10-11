package com.zfl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbOddNum;
import com.zfl.entity.vo.QueryVo;
import com.zfl.jwt.UserJwtService;
import com.zfl.mapper.TbOddNumMapper;
import com.zfl.result.Result;
import com.zfl.service.TbOddNumService;
import com.zfl.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 收单  订单表 服务实现类
 * </p>
 *
 * @author zc
 * @since 2019-08-28
 */
@Service
public class TbOddNumServiceImpl extends ServiceImpl<TbOddNumMapper, TbOddNum> implements TbOddNumService {

    @Autowired
    private TbOddNumMapper tbOddNumMapper;


    @Autowired
    private UserJwtService userJwtService;

    @Override
    public List<TbOddNum> getAllByCondition(QueryVo queryVo) {


        return null;
    }

    @Override
    public List<TbOddNum> getOrderByCondition(HttpServletRequest request,QueryVo<TbOddNum> queryVo) {
        AdminUser user = userJwtService.getUser(request);
        QueryWrapper wrapper = new QueryWrapper();
        //首先判断用户身份
        if(user.getUserType()==1){//此为代理用户，此版本代理即为快递员
            wrapper.eq("pick_up_person",user.getId());
        }else if(user.getUserType()==0){
            wrapper.eq("user_id",user.getId());
        }
        if(StringUtils.isNotBlank(queryVo.getStartTime())){
            //如果只有起始时间,没有结束时间,将当前时间设置为默认时间
            if(StringUtils.isBlank(queryVo.getEndTime())){
                try {
                    queryVo.setEndTime(DateUtils.dateFormat(new Date(),DateUtils.DATE_TIME_PATTERN));
                } catch (ParseException e) {
                }
            }
            wrapper.between("add_time",queryVo.getStartTime(),queryVo.getEndTime());
        }
        if(queryVo.getOrderState1()!=null&&queryVo.getOrderState()!=null){
            wrapper.between("order_state",queryVo.getOrderState(),queryVo.getOrderState1());
        }else{
            if(queryVo.getOrderState()!=null)wrapper.eq("order_state",queryVo.getOrderState());
        }

        TbOddNum oddNum = queryVo.getData();
        if(oddNum!=null&&oddNum.getOrderState() != null){
            wrapper.eq("order_state",oddNum.getOrderState());
        }

        wrapper.orderByDesc("id");
        //设置分页条件
        PageHelper.startPage(queryVo.getPageNo(),queryVo.getPageSize());
        return tbOddNumMapper.selectList(wrapper);
    }

    public Result getOrderNum (HttpServletRequest request){
        int userId = userJwtService.getUserId(request);
        AdminUser user = userJwtService.getUser(request);
        Date date = new Date();
        //获取前一月时间：
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String monthBefore = sdf.format(org.apache.commons.lang3.time.DateUtils.addMonths(date,-1));

        //获取前一周时间：
        String weekBefore = sdf.format(org.apache.commons.lang3.time.DateUtils.addWeeks(date,-1));

        List<TbOddNum> monthBeforeList = tbOddNumMapper.selectList(new QueryWrapper<TbOddNum>().eq("user_id",userId)
                                                                                                .ge("add_time",monthBefore)
                                                                                               .le("add_time",sdf.format(new Date())));
        double remainingSum = user.getRemainingSum();
        if(monthBeforeList.size()==0){
            //若一个月都没有走单，则对他进行提醒

            return new Result(200,"您已经一个月没有订单了","");
        }
        List<TbOddNum> weekBeforeList = tbOddNumMapper.selectList(new QueryWrapper<TbOddNum>().eq("user_id",userId)
                                                                                                .ge("add_time",weekBefore)
                                                                                                .le("add_time",sdf.format(new Date())));
        if(weekBeforeList.size()==0){
            return new Result(200,"您已经一个星期没有订单了",remainingSum);
        }

        return new Result(201,"",remainingSum);
    }

}
