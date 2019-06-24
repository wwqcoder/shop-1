//购物车控制层 
app.controller('cartController',function($scope,cartService,addressService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue=cartService.sum($scope.cartList);
			}
		);		
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		addressService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=addressService.update( $scope.entity ); //修改  
		}else{
			serviceObject=addressService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
					location.href="http://localhost:9107/getOrderInfo.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){ 
					$scope.findCartList();//刷新列表
				}else{
					alert(response.message);//弹出错误提示
				}				
			}
		);
	}
	//获取当前登录用户的收获地址
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				for(var i=0;i<$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=='1'){
						$scope.address=$scope.addressList[i];
						break;
					}
				}
			}
		);
	}
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	//判断某地址对象是否是当前选中的地址
	$scope.isSelectedAddress=function(address){
		if($scope.address==address){
			return true;
		}else{
			return false;
		}
	}
	$scope.order={paymentType:'1'};
	
	//选择支付类型
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	//保存订单
	$scope.submitOrder=function(order){
		
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response.success){
						
						if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
							//页面跳转
							location.href="pay.html";
						}else{//货到付款，跳转到提示页面
							location.href="paysuccess.html";
						}
					}else{
						alert(response.message);
					}
					
				}
		);
	}
	
});
