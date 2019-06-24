app.controller('payController',function($scope,$location,payService){
	
	$scope.createNative=function(){
		payService.createNative().success(
				function(response){
					//显示订单号与金额
					$scope.money=(response.total_fee/100).toFixed(2);
					$scope.out_trade_no=response.out_trade_no;
					
					//生成二维码
				 	var qr = new QRious({
				 		   element:document.getElementById('qrious'),
				 		   size:250,
				 		   level:'H',
				 		   value:response.code_url
				 		});	
				 	queryPayStatus($scope.out_trade_no);//调用查询
				}
		);
	}
	
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
				function(response){
					if(response.success){
						location.href="paysuccess.html#?money="+$scope.money;
					}else{
						if(response.message=="二维码超时"){
							$scope.createNative();//重新生成二维码
						}else{
							location.href="payfail.html";
						}
					}
				}
		);
	}
	
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
	
	
});