package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;
	
	/**
	 * 生成二维码
	 * @return
	 */
	public Map createNative(String out_trade_no,String total_fee){
		//1.创建参数
		Map<String,String> param=new HashMap();//创建参数
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串		
		param.put("body", "品优购");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee",total_fee);//总金额（分）
		param.put("spbill_create_ip", "127.0.0.1");//IP
		param.put("notify_url", "http://www.itcast.cn");//回调地址(随便写)
		param.put("trade_type", "NATIVE");//交易类型
		try {
			//2.生成要发送的xml 
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println(xmlParam);	
			HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();	
			//3.获得结果 
			String result = client.getContent();
			System.out.println(result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);			
			Map<String, String> map=new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));//支付地址
			map.put("total_fee", total_fee);//总金额
			map.put("out_trade_no",out_trade_no);//订单号
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}			
	}

	/**
	 * 查询支付订单状态
	 * @throws Exception 
	 */
	public Map queryPayStatus(String out_trade_no) {
		//1.封装参数
		Map param = new HashMap<>();
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
			//2，发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(paramXml);
			httpClient.post();
			//3.获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("调用查询API返回结果："+map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 关闭订单
	 */
	public Map closePay(String out_trade_no) {
		//1.封装参数
		Map param = new HashMap<>();
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
			//2，发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(paramXml);
			httpClient.post();
			//3.获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("调用查询API返回结果："+map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}




















