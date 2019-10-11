layui.config({
    base: '/static/admin/'
}).extend({
    treetable: 'js/treetable'
}).use(['layer','form','table','treetable'], function() {
    var layer = layui.layer,
        $ = layui.jquery,
        form = layui.form,
        table = layui.table,
        treetable = layui.treetable,
        t;

    t={
        elem: '#authTable',
        url: '/admin/inform/informListAll',
        page: { //支持传入 laypage 组件的所有参数（某些参数除外，如：jump/elem） - 详见文档
            layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'], //自定义分页布局
            //,curr: 5 //设定初始在第 5 页
            groups: 6, //只显示 1 个连续页码
            first: "首页", //显示首页
            last: "尾页", //显示尾页
            limits:[3,10, 20, 30]
        },
        cols: [[
            {field: 'title', title: '标题'},
            {field: 'content', title: '内容'},
            {field: 'time',  title: '通知时间'},
            {field: 'issuer',  title: '发布人'},
            //{templet: '#authBar', align: 'center', title: '操作'}
        ]],
        // done: function () {
        //     layer.closeAll('loading');
        // }
    };
    table.render(t);


    var active={
        addArea : function(){
            var addIndex = layer.open({
                title : "添加通知",
                type : 2,
                area : [ '800px', '700px' ],
                content : "/admin/inform/add",
                success : function(layero, addIndex){
                    setTimeout(function(){
                        layer.tips('点击此处返回列表', '.layui-layer-setwin .layui-layer-close', {
                            tips: 3
                        });
                    },500);
                }
            });
        }
    };

    $('.layui-inline .layui-btn').on('click', function(){
        var type = $(this).data('type');
        active[type] ? active[type].call(this) : '';
    });

    //搜索
    form.on("submit(searchForm)",function(data){
        t.where = data.field;
        table.reload('authTable', t);
        return false;
    });
});