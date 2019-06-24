package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Reference(timeout=6000)
	private CartService cartService;
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		//当前登录人账号
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("登录人："+username);
		
		//从cookie中提取购物车
		System.out.println("从cookie中提取购物车");
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if (username.equals("anonymousUser")) {
			//如果未登录
			return cartList_cookie;
		}else {
			System.out.println("从redis中提取购物车");
			//如果登录
			//获取redis购物车
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if (cartList_cookie.size() > 0) {//判断当本地购物车存在数据
				//合并购物车  得到合并后的购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//将合并后的购物车存入redis
				cartService.saveCartListToRedis(username, cartList);
				//本地购物车清除
				CookieUtil.deleteCookie(request, response, "cartList");
				System.out.println("执行了合并购物车");
				return cartList;
			}
			
			return cartList_redis;
		}
	}

	@RequestMapping("/addGoodsToCartList")
	//@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result  addGoodsToCartList(Long itemId,Integer num) {
		//允许请求的域(当此方法不需要操作cookie)
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		//允许使用cookie
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		//当前登录人账号
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("登录人："+name);
		
		try {
			//1.从cookie中提取购物车
			List<Cart> cartList = findCartList();
			//2.调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if (name.equals("anonymousUser")) {//如果未登录
				System.out.println("向cookie中存入购物车");
				//3.将新的购物车存入cookie
				String cartListString = JSON.toJSONString(cartList);
				CookieUtil.setCookie(request, response, "cartList", cartListString, 3600*24, "UTF-8");
			}else {//如果登录
				
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true, "存入购物车成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "存入购物车失败");
		}
	}
	
}



















