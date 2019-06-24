app.controller('baseController',function($scope){
	//分页控件配置 
	$scope.paginationConf = {
			 currentPage: 1,//当前页
			 totalItems: 10,//总记录数
			 itemsPerPage: 10,//每页显示条数
			 perPageOptions: [10, 20, 30, 40, 50],//分页选项
			 //当页码重新更新时，自动触发的方法
			 onChange: function(){
			      $scope.reloadList();//重新加载
			 }
	}; 
	//重新加载
	$scope.reloadList=function(){
	     $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	$scope.selectIds=[];//用户勾选的id集合
	$scope.updateSelection=function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id);//向集合中添加元素
		}else{
			var index = $scope.selectIds.indexOf(id);//查询得到位置
			$scope.selectIds.splice(index,1);//参数1：移除的位置，参数二:移除的个数
		}
	}
	
	$scope.jsonToString=function(jsonString,key){
		var json=JSON.parse(jsonString);
		var value="";
		
		for(var i = 0 ;i<json.length;i++){
			if(i>0){
				value+=","
			}
			value +=json[i][key];
		}
		return value;
	}
	
	
	
	
});