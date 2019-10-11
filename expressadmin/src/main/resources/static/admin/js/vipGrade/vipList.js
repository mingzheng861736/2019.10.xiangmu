layui.use(['layer','form','table','laydate'], function() {
    var layer = layui.layer,
        $ = layui.jquery,
        form = layui.form,
        table = layui.table,
        laydate = layui.laydate,
        t;                  //表格数据变量
    laydate.render({
        elem: '#startTime',
        type: 'datetime'
    });
    laydate.render({
        elem: '#endTime',
        type: 'datetime'
    });

    t = {
        elem: '#orderTable',
        url:'/admin/vip/getGradeAll',
        method:'post',
        page: { //支持传入 laypage 组件的所有参数（某些参数除外，如：jump/elem） - 详见文档
            layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'], //自定义分页布局
            //,curr: 5 //设定初始在第 5 页
            groups: 6, //只显示 1 个连续页码
            first: "首页", //显示首页
            last: "尾页", //显示尾页
            limits:[3,10, 20, 30]
        },
        width: $(parent.window).width()-218,
        cols: [[
            {field:'vipRank', title: '会员等级'},
            {field:'vipCondition', title: '满足条件' },
            {field:'vipPercentage', title: '收益百分比'},
            {field:'promotionEarnings', title: '推广收益百分比'},
            {field:'addTime',  title: '创建时间'}, //单元格内容水平居中
            {field:'updateTime',  title: '修改时间'},
            {fixed:'right', title: '操作', toolbar: '#orderBar'}
        ]]
    };
    table.render(t);

    //监听工具条
    table.on('tool(vipList)', function(obj){
        console.log(obj)
        var data = obj.data;

        if(obj.event === 'detail'){
            var editIndex = layer.open({
                title : "修改",
                type : 2,
                area: ['35%', '50%'],
                content : "/admin/vip/update?id="+data.id,
                success : function(layero, index){
                    setTimeout(function(){
                        layer.tips('点击此处返回等级列表', '.layui-layer-setwin .layui-layer-close', {
                            tips: 3
                        });
                    },500);
                }
            });
        }
        if(obj.event === "del"){
            layer.confirm("你确定要删除该用户么？",{btn:['是的,我确定','我再想想']},
                function(){
                    $.post("/admin/vip/deleGrade",{"id":data.id},function (res){
                        var code = res.code;
                        if(200 == code){
                            layer.msg("删除成功",{time: 1000},function(){
                                table.reload('orderTable', t);
                            });
                        }else{
                            layer.msg(res.message);
                        }

                    });
                }
            );
        }
    });

    //功能按钮
    var active={
        addUser : function(){
            var addIndex = layer.open({
                title : "添加用户",
                type : 2,
                area: ['35%', '50%'],
                content : "/admin/vip/add",
                success : function(layero, addIndex){
                    setTimeout(function(){
                        layer.tips('点击此处返回会员列表', '.layui-layer-setwin .layui-layer-close', {
                            tips: 3
                        });
                    },500);
                }
            });
        },

    };

    $('.layui-inline .layui-btn').on('click', function(){
        var type = $(this).data('type');
        active[type] ? active[type].call(this) : '';
    });

    //搜索
    form.on("submit(searchForm)",function(data){
        t.where = data.field;
        table.reload('orderTable', t);
        return false;
    });
});