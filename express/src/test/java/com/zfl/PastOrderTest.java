package com.zfl;

import com.zfl.entity.TbOddNum;
import com.zfl.redisservice.RedisService;
import com.zfl.service.TbOddNumService;
import com.zfl.service.expressageService.ExpressageService;
import com.zfl.service.expressageService.SendGetUtil;
import com.zfl.util.DateUtils;
import com.zfl.util.Md5Util;
import com.zfl.util.WxTokenUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @Auther: wbh
 * @Date: 2019/9/5 16:31
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PastOrderTest {
    @Test
    public void a (){
        WxTokenUtil a = new WxTokenUtil();
        String accessToken=a.getAccessToken();
        System.out.println(accessToken);
    }

   
}
