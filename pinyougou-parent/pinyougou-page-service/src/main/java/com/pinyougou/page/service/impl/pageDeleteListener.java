package com.pinyougou.page.service.impl;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class pageDeleteListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;

	public void onMessage(Message message) {
		ObjectMessage objectMessage=(ObjectMessage)message;
		try {
			Long[] goodsIds = (Long[]) objectMessage.getObject();
			boolean b = itemPageService.deleteItemHtml(goodsIds);
			System.out.println("删除静态页"+b);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
