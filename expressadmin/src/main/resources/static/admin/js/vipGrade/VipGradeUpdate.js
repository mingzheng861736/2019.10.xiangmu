layui.use(['form','jquery','layer'],function(){
    var form = layui.form,
        $    = layui.jquery,
        layer = layui.layer;   //默认启用用户

    //表单校验
    form.verify({
        vipRank: function(value, item){ //value：表单的值、item：表单的DOM对象
            if(!new RegExp("^[a-zA-Z\u4e00-\u9fa5\\s·]+$").test(value)){
                return '等级名称只能为中文或英文大小写';
            }
        },
        vipCondition: function(value, item){ //value：表单的值、item：表单的DOM对象
            if(!new RegExp("^[1-9]\\d*$").test(value)){
                return '等级满足条件只能为1-9数字';
            }
        },
        vipPercentage: function(value, item){ //value：表单的值、item：表单的DOM对象
            if(!new RegExp("^0(\\.\\d{1,2})$").test(value)){
                return '收益百分比只能为0-1之间的两位小数，不包括0和1';
            }
        },
        promotionEarnings: function(value, item){ //value：表单的值、item：表单的DOM对象
            if(!new RegExp("^0(\\.\\d{1,2})$").test(value)){
                return '推广收益百分比只能为0-1之间的两位小数，不包括0和1';
            }
        },
    });

    form.on("submit(updateVipGrade)",function(data){
        $.ajax({
            type:"POST",
            url:"/admin/vip/updateGrade",
            dataType:"json",
            contentType:"application/json",
            data:JSON.stringify(data.field),
            success:function(res){
             //   layer.close(loadIndex);
                var code = res.code;
                if(200 == code){
                    parent.layer.msg("用户修改成功!",{time:1500},function(){
                        //刷新父页面
                        parent.location.reload();
                    });
                }else{
                    layer.msg(res.message);
                }
            }
        });
        return false;
    });

    form.on('switch(adminUser)', function(data){
        $("#adminUser").val(data.elem.checked);
    });

    form.on('switch(locked)', function(data){
        $("#locked").val(data.elem.checked);
    });

});