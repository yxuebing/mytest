/** 定义订单控制器 */
app.controller("seckillOrderController", function($scope,$controller,
                   $interval,$location,baseService){

    /** 指定继承seckillGoodsController */
    $controller("seckillGoodsController", {$scope:$scope});


    /** 生成微信支付二维码 */
        $scope.getPayCode=function () {
            baseService.sendGet("/order/getPayCode").then(function (response) {
                        if(response.data){
                            //生成交易订单号
                              $scope.outTradeNo=  response.data.outTradeNo
                            //生成支付金额
                            $scope.money =(response.data.totalFee/100).toFixed(2) ;

                              $scope.codeUrl = response.data.codeUrl ;
                              //生成二维码
                            document.getElementById("qrious").src="/barcode?url="+$scope.codeUrl;

                        }
            })
            // 调用定时器,查询支付状态
            //      $interval   有三个参数
            //   第一个参数   要调用的函数
            // 第二个参数 ,   间隔的时间,毫秒为单位
            // 第三个参数,    要调用的次数

            var timer=  $interval(function () {
                baseService.sendGet("/order/queryPayStatus?outTradeNo="+$scope.outTradeNo).then(function (response) {
                    // 如果状态码为1,代表支付成功, 取消定时器
                    if("1"== response.data.status){
                        $interval.cancel(timer)
                        location.href="/order/paysuccess.html?money="+$scope.money
                    }
                    // 如果状态码为1,代表支付失败, 取消定时器
                    if("3"==response.data.status){
                        $interval.cancel(timer)
                        location.href="/order/payfail.html"
                    }

                })
            },3000,100)
            // 在总次数调用完后,回调一个函数
            timer.then(function () {
                $scope.tip = "二维码已过期,请重新刷新页面 "
            })


        }
     $scope.getMoney=function () {
       return  $location.search().money ;
     }


});