app.controller("brandController",function($scope,$controller,brandService){
			
	$controller('baseController',{$scope:$scope});
	//查询品牌列表
	$scope.findAll=function(){
		brandService.findAll().success(
				function(response){
					$scope.list=response;
				}
		);
	}
	
	
	//分页
	$scope.findPage=function(page,size){
		brandService.findPage(page,size).success(
			function(response){
				//显示当前页的数据
				$scope.list=response.rows;
				//更新总记录数
				$scope.paginationConf.totalItems=response.total;
			}
				
		);
	}
	
	$scope.save=function(){
		var object=null;
		if($scope.entity.id != null){
			object=brandService.update($scope.entity);
		}else{
			object=brandService.add($scope.entity);
		}
		object.success(
				function(response){
					if(response.success){
						 $scope.reloadList();//重新加载
					}else{
						alert(response.message);
					}
				}		
		);
	}
	
	$scope.findOne=function(id){
		brandService.findOne(id).success(
			function(response){
				$scope.entity=response;
			}	
		);
	}
	
	$scope.dele=function(){
		if(confirm('确定要删除吗？')){
			brandService.dele($scope.selectIds).success(
					function(response){
						if(response){
							$scope.reloadList();//重新加载
						}else{
							alert(response.message);
						}
						
					}	
				);
		}
	}
	
	$scope.searchEntity={};
	//条件查询
	$scope.search=function(page,size){
		brandService.search(page,size,$scope.searchEntity).success(
				function(response){
					//显示当前页的数据
					$scope.list=response.rows;
					//更新总记录数
					$scope.paginationConf.totalItems=response.total;
				}
					
			);
	}
});
		