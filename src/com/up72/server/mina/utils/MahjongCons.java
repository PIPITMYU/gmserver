package com.up72.server.mina.utils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2017/6/27.
 */
public class MahjongCons {
    public static Map<Integer,Integer> mahjongType = new TreeMap<Integer,Integer>();
    public static Map<Integer,String> mahjongTypeAlsa = new TreeMap<Integer,String>();
    static {
        mahjongType.put(1,9);
        mahjongType.put(2,9);
        mahjongType.put(3,9);
        mahjongType.put(5,1);

        mahjongTypeAlsa.put(1,"万子牌");
        mahjongTypeAlsa.put(2,"条子牌");
        mahjongTypeAlsa.put(3,"饼子牌");
        mahjongTypeAlsa.put(5,"箭牌");//只有红中
    }
}
