app.controller("itemController",function($scope,$http){
	//存储用户选择的规格
	$scope.specificationItems={};


	//数量的加减
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}

	//用户选择规格
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		searchSku();//查询SKU
	}

	$scope.isSelected=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;	
		}else{
			return false;
		}
	}
	//当前选择的SKU
	$scope.sku={};
	//加载默认的SKU
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
	}
	//匹配两个对象是否相等
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	//	根据查询SKU
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems)){
				$scope.sku=skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'-----',price:0}
	}
	//添加商品到购物车
	$scope.addToCart=function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+
				$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
						function(response){
							if(response.success){
								location.href="http://localhost:9107/cart.html";
							}else{
								alert(response.message);
							}
						}
				);
		
	}


});










		