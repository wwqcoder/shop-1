package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	private TbItemMapper itemMapper;

	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据skuid查询商品明细SKU对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item==null) {
			throw new RuntimeException("商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态不合法");
		}
		//2.根据sku对象得到商家ID
		String sellerId = item.getSellerId();
		//3.根据商家ID在购物车列表中查询购物车对象
		Cart cart = searchCartBySellerId(cartList,sellerId);
		//4.如果购物车列表中不存在该商家的购物车
		if (cart == null) {
			//4.1创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			List<TbOrderItem> orderItemList = new ArrayList<>();
			TbOrderItem orderItem = createOrderItem(num, item);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//4.2将新的购物车对象添加到购物车列表中
			cartList.add(cart);
			
		//5.如果购物车列表中存在该商家的购物车
		}else {
			//判断该商品是否在该购物车的明细列表中存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if (orderItem == null) {
				//5.1如果不存在，创建新的购物车明细列表，并添加到该购物车的明细列表中
				orderItem = createOrderItem(num, item);
				cart.getOrderItemList().add(orderItem);
			}else {
				//5.2如果存在，在原有的数量上添加数量，并更新金额
				orderItem.setNum(orderItem.getNum()+num);//更改数量
				//金额
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
				//当明细的数量<=0移除此明细
				if (orderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(orderItem);
				}
				//当购物车的明细数量为0，在购物车列表中移除此购物车
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);
					
				}
			}
		}
		return cartList;
	}
	/**
	 * 创建一个购物车明细列表
	 * @param num
	 * @param item
	 * @return
	 */
	private TbOrderItem createOrderItem(Integer num, TbItem item) {
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}
	/**
	 * 根据商家ID在购物车列表中查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	/**
	 * 根据itemid，在购物车明细列表中查询购物车明细
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
		for (TbOrderItem orderItem : orderItemList) {
			if (orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 从redis中提取购物车
	 */
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}
	/**
	 * 保存购物车列表到redis
	 */
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存入购物车"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
	}
	/**
	 * 合并购物车
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		//不能简单合并
		for (Cart cart : cartList2) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList1=addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}
}




















