package com.up72.game.service;

import java.util.HashMap;

public interface IPayService {
	Integer findDaiLbyUserId(Long userId);
	Integer findDaiLbyCode(Long code);
	void bindDaiL(HashMap<String, Object> map);
}
