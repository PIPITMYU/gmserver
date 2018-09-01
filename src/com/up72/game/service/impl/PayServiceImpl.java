package com.up72.game.service.impl;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.PayMapper;
import com.up72.game.dao.impl.PayMapperImpl;
import com.up72.game.service.IPayService;
@Service
@Transactional
public class PayServiceImpl implements IPayService{
	private PayMapper payMapper =new PayMapperImpl();
	//根据userId查找绑定代理
	@Override
	public Integer findDaiLbyUserId(Long userId) {
		return payMapper.findDaiLByUserId(userId);
	}
	//根据邀请码查找代理
	@Override
	public Integer findDaiLbyCode(Long code) {
		return payMapper.findDaiLByCode(code);
	}
	@Override
	public void bindDaiL(HashMap<String, Object> map) {
		payMapper.bindDaiL(map);
	}
	
}
