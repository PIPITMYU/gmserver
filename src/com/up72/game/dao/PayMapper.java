package com.up72.game.dao;

import java.util.HashMap;

import org.springframework.stereotype.Repository;
@Repository
public interface PayMapper {
	Integer findDaiLByUserId(Long userId);
	Integer findDaiLByCode(Long code);
	void bindDaiL(HashMap<String, Object> map);
}
