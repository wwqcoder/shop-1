package com.pinyougou.cart.service;
/**
 * 购物车服务接口
 * @author Administrator
 *
 */

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {

	public List<Cart> addGoodsToCartList(List<Cart> list,Long itemId,Integer num);
	
	public List<Cart> findCartListFromRedis(String username);
	
	public void saveCartListToRedis(String username,List<Cart> cartList);
	
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
	
	
}





