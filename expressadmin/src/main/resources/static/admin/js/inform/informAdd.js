var iconShow,$;
layui.use(['form','jquery','layer'],function(){
    var form = layui.form,
        layer = layui.layer;
    $ = layui.jquery;
    form.on("submit(addArea)",function(data){
     /*   var loadIndex = layer.load(2, {
            shade: [0.3, '#333']
        });*/
     console.info(data);

        $.post("/admin/inform/informAdd",data.field,function (res) {
            console.info(data.field);
         /*   layer.close(loadIndex);*/
            if(res.success){
                parent.layer.msg("发送成功!",{time:1500},function(){
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