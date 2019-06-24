package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

/**
 * 支付控制层
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference
	private  WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//提取支付日志(从缓存)
		TbPayLog paylog = orderService.searchPayLogFromRedis(username);
		//调用微信支付接口
		if (paylog != null) {
			return weixinPayService.createNative(paylog.getOutTradeNo(),paylog.getTotalFee()+"");		
		}else {
			return new HashMap<>();
		}
	}
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		
		Result result = null;
		int x = 0;
		while (true) {
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {
				result = new Result(false, "支付发生异常");
				break;	
			}
			if (map.get("trade_state").equals("SUCCESS")){//支付成功
				result = new Result(true, "支付成功");
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			x++;
			if (x >= 100) {
				result = new Result(false, "二维码超时");
				break;
			}
		}
 		return result;
	}
	
}



















