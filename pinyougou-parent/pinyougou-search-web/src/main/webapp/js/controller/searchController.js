app.controller('searchController',function($scope,$location,searchService){
	//定义搜索对象的结构  category:商品分类
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;
				//查询后显示第一页
				//$scope.searchMap.pageNo=1;
				
				//构建分页标签
				buildPageLabel();
			}
		);
	}
	
	buildPageLabel=function(){
		//构建分页标签
		$scope.pageLabel=[];
		var firstPage=1;//开始页码
		var lastPage=$scope.resultMap.totalPages;//截止页码
		//省略号
		$scope.firstDot=true;//开始有点
		$scope.lastDot=true;//结束有点
		
		
		//如果页码数量大于5
		if($scope.resultMap.totalPages>5){
			//如果当前页码<=3，显示前5页
			if($scope.searchMap.pageNo<=3){
				lastPage=5;
				$scope.firstDot=false;//前面无点
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
				//显示后5页
				firstPage=$scope.resultMap.totalPages-4;
				$scope.lastDot=false;//后面无点
			}else{
				//显示以当前页中心中的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后面无点
		}
		//构建页码
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		
		if(key=='category' || key=='brand' || key=='price'){
			//如果用户点击的是分类或者品牌
			$scope.searchMap[key]=value;
		}else{
			//用户点击的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//查询
	}
	
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){
			//如果用户点击的是分类或者品牌
			$scope.searchMap[key]="";
		}else{
			//用户点击的是规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
	//分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//查询
	}
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		
		$scope.search();//查询
	}
	//判断关键字是否是品牌
	$scope.keywordsIdBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	
	$scope.loadKeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();//查询
	}
	
});
















