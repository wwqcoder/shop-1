app.controller('seckillGoodsController',function($scope,$location,$interval,seckillGoodsService){
	 //读取列表数据绑定到表单中  
	$scope.findList=function(){
		seckillGoodsService.findList().success(
			function(response){
				$scope.list=response;
			}			
		);
	}     
	$scope.findOne=function(id){
		seckillGoodsService.findOne($location.search()['id']).success(
				function(response){
					$scope.entity=response;
					//倒计时开始
					//获取结束时间到当前日期的秒数
					allsecond= Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
					time=$interval(function(){
						allsecond=allsecond-1;
						$scope.timeString=convertTimeString(allsecond);
						if(allsecond<=0){
							$interval.cancel(time);
						}
					},1000);
				}
		);
	}
	//转换秒为小时格式
	convertTimeString=function(allsecond){
		var days=Math.floor(allsecond/(60*60*24));//天数
		var hours=Math.floor((allsecond-days*60*60*24)/(60*60));//小时数
		var minutes=Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟数
		var seconds=allsecond-days*60*60*24-hours*60*60-minutes*60;//秒数
		var timeString = "";
		if(days > 0){
			timeString=days+"天";
		}
		
		
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){
						alert("抢购成功，请在5分钟内完成支付");
						//下单成功
						location.href="pay.html";//跳转到支付页面
					}else{
						alert(response.message);
					}
				}
		);
	}
	
	
});













