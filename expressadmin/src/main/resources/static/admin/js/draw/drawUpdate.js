var iconShow,$;
layui.use(['form','jquery','layer','upload'],function(){
    var form = layui.form,
        layer = layui.layer,
        upload = layui.upload;
    $    = layui.jquery;


    form.on("submit(updateDraw)",function(data){
        if(data.field.id == null){
            layer.msg("ID不存在",{time:1000});
            return false;
        }
        var loadIndex = layer.load(2, {
            shade: [0.3, '#333']
        });
        console.log(data);
        $.post("/admin/draw/updateDraw",data.field,function (res) {
            layer.close(loadIndex);
            if(res.success){
                parent.layer.msg("审核成功!",{time:1500},function(){
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