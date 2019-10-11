var iconShow,$;
layui.use(['form','jquery','layer','upload','layedit'],function(){
    var form = layui.form,
        layer = layui.layer,
        upload = layui.upload,
        layedit = layui.layedit;
    $    = layui.jquery;

    //富文本编辑器
    var index = layedit.build('goodsInfo',{//建立编辑器
        height: 180 //设置编辑器高度
        ,hideTool:['image','face','link']
    });
    form.verify({
        node_content: function(value) {
            layedit.sync(index);
        }
    });

    //监听是否预支radio
    form.on('radio(isAdvance)', function(data){
        console.log(data.elem); //得到radio原始DOM对象
        console.log(data.value); //被点击的radio的value值
        if(data.value==1){
            $("#minConvertibilityId").addClass("layui-hide");
            $("#minConvertibilityId").removeProp("lay-verify");
            $("#goodsStagingPeriodId").addClass("layui-hide");
            $("#goodsStagingPeriodId").removeProp("lay-verify");
            $("#goodsMoneyPriceId").addClass("layui-hide");
            $("#goodsMoneyPriceId").removeProp("lay-verify");
            //把参数置空
            $("#goodsMoneyPrice").val(0);
            $("#minConvertibility").val(0);
        }else{
            $("p").removeClass("intro");
            $("#minConvertibilityId").removeClass("layui-hide");
            $("#minConvertibilityId").prop("lay-verify","required");
            $("#goodsStagingPeriodId").removeClass("layui-hide");
            $("#goodsStagingPeriodId").prop("lay-verify","required");
            $("#goodsMoneyPriceId").removeClass("layui-hide");
            $("#goodsMoneyPriceId").prop("lay-verify","required");

        }
    });

    //执行实例
    var uploadInst = upload.render({
        elem: '#uploadImg' //绑定元素
        ,url: '/admin/img/uploadfile2/' //上传接口
        ,accept: 'images'
        ,size: 0
        ,done: function(res, index, upload){
            //上传完毕回调
            console.log("上传完了"+res);
            console.log(layui.$.fn.jquery);
            if(res.success){
                // console.log($('#showImage'))
                $('#imagesHid').val(res.data.url);
                document.getElementById('imagesHid').value=res.data.url;
                $('#showImage').attr('src', res.data.url);
                $('#showImage').css("margin-top","15px");
                layer.msg(res.message);
            }else{
                layer.msg(res.message);
            }
        }
        ,error: function(){
            //请求异常回调
        }
    });

    form.on("submit(editTrack)",function(data){
        if(data.field.id == null){
            layer.msg("ID不存在",{time:1000});
            return false;
        }
        var loadIndex = layer.load(2, {
            shade: [0.3, '#333']
        });
        data.field.
        $.post("/admin/goods/edit",data.field,function (res) {
            layer.close(loadIndex);
            if(res.success){
                parent.layer.msg("编辑成功!",{time:1500},function(){
                    //刷新父页面
                    parent.location.reload();
                });
            }else{
                layer.msg(res.message);
            }
        });
        return false;
    });

});