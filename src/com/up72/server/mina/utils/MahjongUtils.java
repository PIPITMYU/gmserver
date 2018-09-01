package com.up72.server.mina.utils;

import java.nio.channels.ShutdownChannelGroupException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.utils.JudegHu.checkHu.Hulib;
import com.up72.server.mina.utils.JudegHu.checkHu.TableMgr;

/**
 * Created by Administrator on 2017/6/29.
 */
public class MahjongUtils {

	static {
		// 加载胡的可能
		TableMgr.getInstance().load();
	}

	/**
	 * 排序 给出一组牌 返回按照类型以及大小拍好顺序的牌
	 * 
	 * @param pais
	 * @return
	 */
	public static List<Integer[][]> paiXu(List<Integer[][]> pais) {
		Integer[] arrays = new Integer[pais.size()];
		for (int i = 0; i < arrays.length; i++) {
			arrays[i] = pais.get(i)[0][0] * 10 + pais.get(i)[0][1];
		}
		Arrays.sort(arrays);
		for (int i = 0; i < arrays.length; i++) {
			pais.get(i)[0][0] = arrays[i] / 10;
			pais.get(i)[0][1] = arrays[i] % 10;
		}
		return pais;
	}

	public static void main(String[] args) {
		// test1
		 List<Integer[][]> list = new ArrayList<Integer[][]>();
		 list.add(new Integer[][] { { 1, 1 } });
		 list.add(new Integer[][] { { 1, 2 } });
		 list.add(new Integer[][] { { 1, 3 } });
		 list.add(new Integer[][] { { 1, 4 } });
		 list.add(new Integer[][] { { 1, 5 } });
		 list.add(new Integer[][] { { 1, 6 } });
		 list.add(new Integer[][] { { 1, 7 } });
		 list.add(new Integer[][] { { 1, 8 } });
		 list.add(new Integer[][] { { 1, 9 } });
		 list.add(new Integer[][] { { 2, 1 } });
		 list.add(new Integer[][] { { 2, 2 } });
		 list.add(new Integer[][] { { 2, 3 } });
//		 list.add(new Integer[][] { { 3, 1 } });
//		 list.add(new Integer[][] { { 3, 2 } });
//		 list.add(new Integer[][] { { 3, 3 } });
		 list.add(new Integer[][] { { 5, 1 } });
		
		 Player chuPlayer= new Player();
		 chuPlayer.setCurrentMjList(list);
		 System.out.println(checkTing(chuPlayer));
	}

	/**
	 * 检测是否胡
	 * 
	 * @param p
	 * @param pai
	 * @return
	 */
	public static boolean checkHuNew(Player p, Integer[][] pai) {
		List<Integer[][]> tempShouPai = getNewList(p.getCurrentMjList());
		int[] paiArray = getShouPaiArray(p.getCurrentMjList(), pai);
		if (pai != null) {
			tempShouPai.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		paiXu(tempShouPai);
		// 检测是不是七对 --七对的时候可以缺门，可以却1，9
		if (tempShouPai.size() == 14) {
			if (checkQiDui(paiArray, 14)) {
				return true;
			}
		}
		// 检测是不是清一色，清一色需要1,9和叉，不需要3色全
		if (isQingYiSe(p, pai)) {
			return true;
		}
		// 别的牌型不能缺幺九，缺门，必须有叉牌
		if (!checkHuRule(p, tempShouPai, pai, paiArray)) {
			return false;
		}
		// 满足检测是否胡
		/** 如果哪个牌的个数是0，就让guiIndex是这个 */
		boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, 34);
		return hu;
	}

	/**
	 * 检测手牌能否胡，此方法不管是否有1，9，三门和叉牌
	 * 
	 * @param shouPaiList
	 *            手牌集合
	 * @return
	 */
	public static boolean checkHuNewShouPais(List<Integer[][]> shouPaiList) {
		int[] paiArray = getShouPaiArray(shouPaiList, null);
		return Hulib.getInstance().get_hu_info(paiArray, 34, 34);
	}

	/**
	 * 确保手牌不能缺幺九（有1或者9的一个就可以），缺门，必须有叉牌（有过碰和杠或者 手中排除3张一样后剩下的牌可以胡牌）
	 * 
	 * @param tempShouPai
	 * 
	 * @param tempShouPai
	 *            加上赢的那张牌的手牌
	 * @param paiArray
	 * @return
	 */
	private static boolean checkHuRule(Player p, List<Integer[][]> newList,
			Integer[][] pai, int[] paiArray) {
		// 用于存出玩家所有动作的牌型

		Boolean hasChaOrGang = false;
		Boolean hasYiJiu = false;
		// list2用于存储所有的牌
		Set<Integer> paiNumList = new HashSet<Integer>();
		// 用于存储所有牌的大小（比如一万一并一条存1，5万5饼5条存5）
		Set<Integer> paiXingList = new HashSet<Integer>();
		for (int i = 0; i < newList.size(); i++) {
			if (!newList.get(i)[0][0].equals(5)) {// 不加红中的排数
				paiNumList.add(newList.get(i)[0][1]);
			}
			paiXingList.add(newList.get(i)[0][0]);
		}
		List<InfoCount> chiList = p.getChiList();
		List<InfoCount> pengList = p.getPengList();
		List<InfoCount> gangListType4 = p.getGangListType4();
		List<InfoCount> gangListType3 = p.getGangListType3();
		List<InfoCount> gangListType5 = p.getGangListType5();

		// 获取手牌的类型
		if (chiList != null && chiList.size() > 0) {
			for (InfoCount infoCount : chiList) {
				// 获取吃的类型
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				// 获取吃的大小
				paiNumList.add(infoCount.getL().get(0)[0][1]);
				paiNumList.add(infoCount.getL().get(1)[0][1]);
				paiNumList.add(infoCount.getL().get(2)[0][1]);
			}
		}
		if (pengList != null && pengList.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : pengList) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType4 != null && gangListType4.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType4) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType3 != null && gangListType3.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType3) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType5 != null && gangListType5.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType5) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		// 检测1,9 牌型大小满足1或9，类型满足5（红中可以顶1，9）
		if (paiNumList.contains(1) || paiNumList.contains(9)
				|| paiXingList.contains(5)) {
			hasYiJiu = true;
		}
		if (!hasYiJiu) {
			return false;
		}

		// 包含红中免叉
		if (paiXingList.contains(5)) {
			hasChaOrGang = true;
		}
		// 检测叉
		// 如果没有碰和杠，检测手牌中是否满足叉
		if (!hasChaOrGang) {
			if (checkChaInShouPai(paiArray)) {
				hasChaOrGang = true;
			}
		}
		if (!hasChaOrGang) {
			return false;
		}
		paiXingList.remove(5);// 移除红中
		if (paiXingList.size() != 3) {
			return false;
		}
		return true;
	}

	/**
	 * 检测手牌中是否有叉牌
	 * 
	 * @param paiArray
	 * @return
	 */
	private static Boolean checkChaInShouPai(int[] paiArray) {
		for (int i = 0; i < paiArray.length; i++) {
			if (paiArray[i] >= 3) {
				paiArray[i] = paiArray[i] - 3;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					paiArray[i] = paiArray[i] + 3;
					return true;
				}
				paiArray[i] = paiArray[i] + 3;
			}
		}
		return false;
	}

	static List<Integer[][]> getNewList(List<Integer[][]> old) {
		List<Integer[][]> newList = new ArrayList<Integer[][]>();
		if (old != null && old.size() > 0) {
			for (Integer[][] pai : old) {
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			}
		}
		return newList;
	}

	/**
	 * 获取不重复的List集合
	 * 
	 * @param shouPaiList
	 * @return
	 */
	private static ArrayList<Integer[][]> getDistinct(
			List<Integer[][]> shouPaiList) {
		if (shouPaiList != null && shouPaiList.size() > 0) {
			Set<String> disSet = new HashSet<>();
			ArrayList<Integer[][]> list = new ArrayList<>();
			for (Integer[][] pai : shouPaiList) {
				if (!disSet.contains(pai[0][0] + "_" + pai[0][1])) {
					disSet.add(pai[0][0] + "_" + pai[0][1]);
					list.add(new Integer[][] { { pai[0][0], pai[0][1] } });
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * 产生如下的数组，去检测是否胡牌 int[] cards = { 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0,
	 * 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	 * 如果牌为null则判断自摸 如果不是null，则判断点炮
	 * 
	 * @param playersPais
	 * @param pai
	 * @return
	 */
	private static int[] getShouPaiArray(List<Integer[][]> playersPais,
			Integer[][] pai) {
		int[] array = initArrays();
		for (int i = 0; i < playersPais.size(); i++) {
			addArray(array, playersPais.get(i));
		}
		if (pai != null) {
			addArray(array, pai);
		}
		return array;
	}

	/**
	 * length为34的新数组
	 * 
	 * @return
	 */
	private static int[] initArrays() {
		int[] array = new int[34];
		for (int i = 0; i < 34; i++) {
			array[i] = 0;
		}
		return array;
	}

	private static void addArray(int[] array, Integer[][] pai) {
		int type = pai[0][0];
		int num = pai[0][1];
		switch (type) {
		case 1:
		case 2:
		case 3:
		case 4:
			array[(type - 1) * 9 + num - 1]++;
			break;
		case 5:
			array[3 * 9 + 4 + num - 1]++;
			break;
		}
	}

	/**
	 * 初始化麻将 红中设置为{5,1}
	 * 
	 * @return
	 */
	public static List<Integer[][]> initMahjongs() {
		List<Integer[][]> list = new ArrayList<>();
		for (Integer type : MahjongCons.mahjongType.keySet()) {
			for (int i = 0; i < MahjongCons.mahjongType.get(type); i++) {
				for (int j = 0; j < 4; j++) {
					list.add(new Integer[][] { { type, i + 1 } });
				}
			}
		}
		return list;
	}

	/**
	 * 洗牌 传入一副麻将，打乱顺序之后，返回麻将
	 * 
	 * @param mahjongs
	 * @return
	 */
	public static List<Integer[][]> xiPai(List<Integer[][]> mahjongs) {
		List<Integer[][]> temp = new ArrayList<>();
		int last = mahjongs.size();
		int random = 0;
		for (int i = 0; i < mahjongs.size(); i++) {
			random = (int) (Math.random() * last);
			temp.add(new Integer[][] { { mahjongs.get(random)[0][0],
					mahjongs.get(random)[0][1] } });
			mahjongs.remove(random);
			i--;
			last = mahjongs.size();
		}
		return temp;
	}

	/**
	 * 发牌/揭牌 传入麻将列表，以及要发几张牌，返回对应的数组 如果牌数少于要求返回的张数，返回null
	 * 
	 * @param mahjongs
	 * @param num
	 * @return
	 */
	public static List<Integer[][]> faPai(List<Integer[][]> mahjongs,
			Integer num) {
		if (mahjongs.size() == 0) {
			return null;
		}
		List<Integer[][]> result = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			result.add(new Integer[][] { { mahjongs.get(i)[0][0],
					mahjongs.get(i)[0][1] } });
			mahjongs.remove(i);
		}
		if (num > 1) {
			result = paiXu(result);
		}
		return result;
	}

	/**
	 * 红中麻将 检测胡的类型
	 * 
	 * @param p
	 *            赢得玩家
	 * @param chuUser
	 *            出牌的玩家
	 * @param pai
	 *            最后的动作牌（自己摸的，或者别人出的）
	 * @return
	 */
	public static Map<String, HashMap<Integer, Integer>> checkHuInfo(Player p,
			Player chuUser, Integer[][] pai, List<Player> players, RoomResp room) {
		List<Integer[][]> currentMjList = p.getCurrentMjList();
		// 获取手牌集合
		List<Integer[][]> newList = getNewList(currentMjList);
		if (pai != null) {
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		// paiXu(newList);
		int[] paiArray = getShouPaiArray(p.getCurrentMjList(), pai);

		HashMap<String, HashMap<Integer, Integer>> map = new HashMap<String, HashMap<Integer, Integer>>();
		// 此map的分数是相加的：单吊。夹胡，对倒，七小对，飘，平胡
		HashMap<Integer, Integer> fenMap = new HashMap<>();
		// 此map的分数是与分数map的和相乘的：清一色，三家清，四家清，刚上开花，杠后点炮，抢杠胡，四归一
		HashMap<Integer, Integer> fanMap = new HashMap<>();
		Integer result = 0;
		// 检测是否红中
		if (pai[0][0].equals(5)) {
			fenMap.put(Cnst.HUTYPE_HONGZHONG, Cnst.HU_FAN__HONGZHONG);
		}

		// 是否算黄庄分
		if (room.getLastHuang() == true) {
			fanMap.put(Cnst.HUTYPE_LIANZHUANG, Cnst.HU_FAN_LIANZHUANG);
			room.setLastHuang(false);
		}
		// 检测是否自摸
		if (p.getUserId().equals(chuUser.getUserId())) {
			// 检测刚上开花
			if (!p.getHasGang().equals(0)) {
				if (p.getHasGang().equals(1)) {
					fanMap.put(Cnst.HUTYPE_MINGGANGSHANGKAIHUA,
							Cnst.HU_FAN_MINGGANGSHANGKAIHUA);
				} else {// 肯定是2
					fanMap.put(Cnst.HUTYPE_ANGANGSHANGKAIHUA,
							Cnst.HU_FAN_ANGANGSHANGKAIHUA);
				}
			}
		} else {
			// 检测杠后点炮
			if (!chuUser.getHasGang().equals(0)) {
				if (chuUser.getHasGang().equals(1)) {
					fanMap.put(Cnst.HUTYPE_MINGGANGHOUDIANPAO,
							Cnst.HU_FAN_MINGGANGHOUDIANPAO);
				} else {// 肯定是2
					fanMap.put(Cnst.HUTYPE_ANGANGHOUDIANPAO,
							Cnst.HU_FAN_ANGANGHOUDIANPAO);
				}
			}
			// 检测抢杠胡
			if (room.getLastPengGangUser() != null) {
				fanMap.put(Cnst.HUTYPE_QIANGGANGHU, Cnst.HU_FAN_QIANGGANGHU);
			}
		}

		boolean notPinghu = false;
		// 七小对——-32 飘胡——-16 单吊——-4 夹胡——-4 对倒——-2 平胡——-2
		// 检测七小对
		boolean qidui = false;
		if (newList.size() == 14) {
			if (checkQiDui(paiArray, 14)) {// 满足七小对
				notPinghu = true;
				qidui = true;
				fenMap.put(Cnst.HUTYPE_QIXIAODUI, Cnst.HU_FAN_QIXIAODUI);
			}
		}
		if (!qidui) {// 七对分最高
			// 获取牌的位置
			int place = findPlaceInArray(pai);
			if (paiArray[place] >= 2) {//
				paiArray[place] = paiArray[place] - 2;
				boolean isDiao = false;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					isDiao = true;
				}
				paiArray[place] = paiArray[place] + 2;
				if (isDiao) {// 能胡，说明满足吊
					// 检测飘胡 --剩下1张牌时，也是单吊
					if (isPiao(p, paiArray)) {// 满足飘(飘的分大于单吊，所以满足飘就不检测单调了)
						notPinghu = true;
						fenMap.put(Cnst.HUTYPE_PIAO, Cnst.HU_FAN_PIAO);
					} else {
						// 检测是否单吊
						result = checkDanDiao(paiArray, pai, place);
						if (result.equals(1)) {
							notPinghu = true;
							fenMap.put(Cnst.HUTYPE_DANDIAO, Cnst.HU_FAN_DANDIAO);
						}
					}
				} else {// 不是吊，检测是否对倒
					if (checkDuiDao(paiArray, place, pai)) {
						notPinghu = true;
						// 检测是不是飘
						if (isPiao(p, paiArray)) {// 飘分大
							fenMap.put(Cnst.HUTYPE_PIAO, Cnst.HU_FAN_PIAO);
						} else {// 加对倒分
							fenMap.put(Cnst.HUTYPE_DUIDAO, Cnst.HU_FAN_DUIDAO);
						}
					}
				}
			}
			// 检测单吊
			// 检测是不是夹胡
			if (!notPinghu) {// 如果上面的不满足，检测夹胡
				if (checkJia(paiArray, place, pai)) {
					notPinghu = true;
					fenMap.put(Cnst.HUTYPE_JIAHU, Cnst.HU_FAN_JIAHU);
				}
			}
		}
		// 平胡
		if (!notPinghu) {
			fenMap.put(Cnst.HUTYPE_PINGHU, Cnst.HU_FAN_PINGHU);
		}
		// 检测清一色
		if (isQingYiSe(p, pai)) {
			fanMap.put(Cnst.HUTYPE_QINGYISE, Cnst.HU_FAN_QINGYISE);
		}
		// 检测三家清--暂时指其他三家是都是门清
		int otherNum = 0;
		int myNum = 0;
		for (Player player : players) {
			if (p.getUserId().equals(player.getUserId())) {// 是自己
				if (isMenQing(p)) {
					myNum++;
				}
			} else {
				if (isMenQing(player)) {
					otherNum++;
				}
			}
		}
		if (otherNum == 3) {// 三家清
			// 检测四家清
			if (myNum == 1) {// 三家清 和四家清不能共存
				fanMap.put(Cnst.HUTYPE_SIJIAQING, Cnst.HU_FAN_SIJIAQING);
			} else {
				fanMap.put(Cnst.HUTYPE_SANJIAQING, Cnst.HU_FAN_SANJIAQING);
			}
		}
		// 检测四归一
		Integer siGuiYiNum = checkSiGuiYi(p, pai);
		if (siGuiYiNum > 0) {
			fanMap.put(Cnst.HUTYPE_SIGUIYI, Cnst.HU_FAN_SIGUIYI * siGuiYiNum);
		}
		// // 如果有七小对，就不要单吊了
		// if (fenMap.containsKey(Cnst.HUTYPE_QIXIAODUI)) {// 七小对，不要单吊红中和单吊
		// fenMap.remove(Cnst.HUTYPE_DANDIAO);
		// } else if (fenMap.containsKey(Cnst.HUTYPE_PIAO)) {// 飘的话，不需要对到和单吊
		// fenMap.remove(Cnst.HUTYPE_DUIDAO);
		// fenMap.remove(Cnst.HUTYPE_DANDIAO);
		// }
		// 获取碰的番数
		List<InfoCount> pengList = p.getPengList();
		// 明杠
		List<InfoCount> gangListType3 = p.getGangListType3();
		List<InfoCount> gangListType4 = p.getGangListType4();
		// 暗杠
		Integer anGangFan = 0;
		Integer mingGangFan = 0;
		List<InfoCount> gangListType5 = p.getGangListType5();
		if (pengList != null && pengList.size() > 0) {
			for (InfoCount infoCount : pengList) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是碰红中
					fanMap.put(Cnst.HUTYPE_PENG_HONGZHONG_FAN,
							Cnst.PENG_HONGZHONG_FAN);
				}
			}
		}
		if (gangListType3 != null && gangListType3.size() > 0) {
			for (InfoCount infoCount : gangListType3) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是杠红中
					fanMap.put(Cnst.HUTYPE_GANG_HONGZHONG_MING,
							Cnst.GANG_HONGZHONG_MING);
				} else {// 普通杠
					fanMap.put(Cnst.HUTYPE_GANG_TYPE_MING,
							mingGangFan += Cnst.GANG_TYPE_MING);
				}
			}
		}
		if (gangListType4 != null && gangListType4.size() > 0) {
			for (InfoCount infoCount : gangListType4) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是杠红中
					fanMap.put(Cnst.HUTYPE_GANG_HONGZHONG_MING,
							Cnst.GANG_HONGZHONG_MING);
				} else {// 普通杠
					fanMap.put(Cnst.HUTYPE_GANG_TYPE_MING,
							mingGangFan += Cnst.GANG_TYPE_MING);
				}
			}
		}
		if (gangListType5 != null && gangListType5.size() > 0) {
			for (InfoCount infoCount : gangListType5) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是暗杠红中
					fanMap.put(Cnst.HUTYPE_GANG_HONGZHONG_AN,
							Cnst.GANG_HONGZHONG_AN);
				} else {// 普通杠
					fanMap.put(Cnst.HUTYPE_GANG_TYPE_AN,
							anGangFan += Cnst.GANG_TYPE_AN);
				}
			}
		}
		if(p.getKouTing()==2){
			fanMap.put(Cnst.HUTYPE_KOU,
					anGangFan += Cnst.HU_FAN_KOU);
		}
		map.put("fenMap", fenMap);
		map.put("fanMap", fanMap);
		return map;
	}

	/**
	 * 检测四归一 吃碰和手牌中 只要有4张一样的就加一次(杠排除)
	 * 
	 * @param p
	 * @param pai
	 * @return
	 */
	private static Integer checkSiGuiYi(Player p, Integer[][] pai) {
		Integer siGuiYiNum = 0;
		List<Integer[][]> currentMjList = p.getCurrentMjList();
		List<Integer[][]> newList = getNewList(currentMjList);
		if (pai != null) {
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		List<InfoCount> chiList = p.getChiList();
		List<InfoCount> pengList = p.getPengList();
		if (chiList != null && chiList.size() > 0) {
			for (InfoCount infoCount : chiList) {
				List<Integer[][]> l = infoCount.getL();
				for (Integer[][] integers : l) {
					newList.add(new Integer[][] { { integers[0][0],
							integers[0][1] } });
				}
			}
		}
		if (pengList != null && pengList.size() > 0) {
			for (InfoCount infoCount : pengList) {
				List<Integer[][]> l = infoCount.getL();
				for (Integer[][] integers : l) {
					newList.add(new Integer[][] { { integers[0][0],
							integers[0][1] } });
				}
			}
		}
		paiXu(newList);
		ArrayList<Integer[][]> distinct = getDistinct(newList);
		for (Integer[][] integers : distinct) {
			int num = 0;
			for (int i = 0; i < newList.size(); i++) {
				if (integers[0][0].equals(newList.get(i)[0][0])
						&& integers[0][1].equals(newList.get(i)[0][1])) {
					num++;
				}
			}
			if (num == 4) {
				siGuiYiNum++;
			}
		}
		return siGuiYiNum;
	}

	// 检测是否门清
	public static Boolean isMenQing(Player p) {
		if ((p.getChiList() == null || p.getChiList().size() == 0)
				&& (p.getPengList() == null || p.getPengList().size() == 0)
				&& (p.getGangListType4() == null || p.getGangListType4().size() == 0)
				&& (p.getGangListType3() == null || p.getGangListType3().size() == 0)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param p
	 *            要检测的玩家
	 * @param pai
	 * @return 清一色 ，0不满足
	 */
	public static boolean isQingYiSe(Player p, Integer[][] pai) {
		int[] paiArray = getShouPaiArray(p.getCurrentMjList(), pai);

		List<Integer[][]> currentMjList = p.getCurrentMjList();
		List<Integer[][]> newList = getNewList(currentMjList);

		Boolean hasChaOrGang = false;
		Boolean hasYiJiu = false;
		if (pai != null) {
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		paiXu(newList);
		// list2用于存储所有的牌
		List<Integer> paiNumList = new ArrayList<>();
		// 用于存储所有牌的大小（比如一万一并一条存1，5万5饼5条存5）
		Set<Integer> paiXingList = new HashSet<Integer>();
		for (int i = 0; i < newList.size(); i++) {
			if (!newList.get(i)[0][0].equals(5)) {// 不加红中
				paiNumList.add(newList.get(i)[0][1]);
			}
			paiXingList.add(newList.get(i)[0][0]);
		}
		List<InfoCount> chiList = p.getChiList();
		List<InfoCount> pengList = p.getPengList();
		List<InfoCount> gangListType4 = p.getGangListType4();
		List<InfoCount> gangListType3 = p.getGangListType3();
		List<InfoCount> gangListType5 = p.getGangListType5();

		// 获取手牌的类型
		if (chiList != null && chiList.size() > 0) {
			for (InfoCount infoCount : chiList) {
				// 获取吃的类型
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				// 获取吃的大小
				paiNumList.add(infoCount.getL().get(0)[0][1]);
				paiNumList.add(infoCount.getL().get(1)[0][1]);
				paiNumList.add(infoCount.getL().get(2)[0][1]);
			}
		}
		if (pengList != null && pengList.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : pengList) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType4 != null && gangListType4.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType4) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType3 != null && gangListType3.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType3) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		if (gangListType5 != null && gangListType5.size() > 0) {
			hasChaOrGang = true;
			for (InfoCount infoCount : gangListType5) {
				paiXingList.add(infoCount.getL().get(0)[0][0]);
				paiNumList.add(infoCount.getL().get(0)[0][1]);
			}
		}
		// 检测1，9 牌型大小满足1或9，类型满足5（红中可以顶1，9）
		if (paiNumList.contains(1) || paiNumList.contains(9)
				|| paiXingList.contains(5)) {
			hasYiJiu = true;
		}
		if (hasYiJiu == false) {
			return false;
		}
		// 包含红中免叉
		if (paiXingList.contains(5)) {
			hasChaOrGang = true;
		}
		// 检测叉
		// 如果没有碰和杠，检测手牌中是否满足叉
		if (!hasChaOrGang) {
			if (checkChaInShouPai(paiArray)) {
				hasChaOrGang = true;
			}
		}
		if (!hasChaOrGang) {
			return false;
		}
		// 牌型移除红中
		paiXingList.remove(5);
		if (paiXingList.size() > 1) {
			return false;
		}
		// 检测此牌可否胡
		return Hulib.getInstance().get_hu_info(paiArray, 34, 34);
	}

	/**
	 * 检测是不是夹胡如 4 6 夹5 特殊：12胡3 89胡7也算夹 如果是3和7的需要额外
	 * 
	 * @param p
	 * @param pai
	 * @return
	 */
	public static boolean checkJia(int[] paiArray, int place, Integer[][] pai) {
		// 红中肯定不满足夹胡
		if (pai[0][0].equals(5)) {
			return false;
		}
		// 说明是万饼条
		// 1:移除发的牌
		paiArray[place] = paiArray[place] - 1;
		// 检测普通夹胡 2 -8
		if (pai[0][1] < 9 && pai[0][1] > 1) {
			// 找到两边的牌
			if (paiArray[place - 1] > 0 && paiArray[place + 1] > 0) {
				// 移除这两张牌
				paiArray[place - 1] = paiArray[place - 1] - 1;
				paiArray[place + 1] = paiArray[place + 1] - 1;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					paiArray[place - 1] = paiArray[place - 1] + 1;
					paiArray[place + 1] = paiArray[place + 1] + 1;
					paiArray[place] = paiArray[place] + 1;
					return true;
				}
				// 说明不满足
				paiArray[place - 1] = paiArray[place - 1] + 1;
				paiArray[place + 1] = paiArray[place + 1] + 1;
			}
		}
		// 如果是3和7 需要再次做特殊检测
		if (pai[0][1].equals(3)) {
			// 找1和2
			if (paiArray[place - 1] > 0 && paiArray[place - 2] > 0) {
				// 移除这两张牌
				paiArray[place - 1] = paiArray[place - 1] - 1;
				paiArray[place - 2] = paiArray[place - 2] - 1;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					paiArray[place - 1] = paiArray[place - 1] + 1;
					paiArray[place - 2] = paiArray[place - 2] + 1;
					paiArray[place] = paiArray[place] + 1;
					return true;
				}
				// 说明不满足
				paiArray[place - 1] = paiArray[place - 1] + 1;
				paiArray[place - 2] = paiArray[place - 2] + 1;
			}
		} else if (pai[0][1].equals(7)) {
			if (paiArray[place + 1] > 0 && paiArray[place + 2] > 0) {
				// 移除这两张牌
				paiArray[place + 1] = paiArray[place + 1] - 1;
				paiArray[place + 2] = paiArray[place + 2] - 1;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					paiArray[place + 1] = paiArray[place + 1] + 1;
					paiArray[place - 2] = paiArray[place + 2] + 1;
					paiArray[place] = paiArray[place] + 1;
					return true;
				}
				// 说明不满足
				paiArray[place + 1] = paiArray[place + 1] + 1;
				paiArray[place + 2] = paiArray[place + 2] + 1;
			}
		}
		return false;
	}

	/**
	 * 检测是不是对倒 移除这两个对子能胡 首先，必须有至少两种（含有相同牌两张或以上） 满足：1 ：首先发的牌，自己至少含有两张，
	 * 2：另外含有两张以上的牌,切移除发的那张，再移除此两张能胡
	 * 
	 * @param newList
	 * @param pai
	 * @return
	 */
	public static boolean checkDuiDao(int[] paiArray, int place, Integer[][] pai) {
		Integer num = 0;

		num = paiArray[place];
		// paiArray中包含手牌，所以 必须有3张以上此牌
		if (num < 3) {
			return false;
		}
		// 移除发的那张牌
		paiArray[place] = paiArray[place] - 1;
		// 检测是否还有别的两张以上的牌,如果移除能胡，那么满足
		for (int i = 0; i < paiArray.length; i++) {
			if (paiArray[i] >= 2) {// 有两张
				paiArray[i] = paiArray[i] - 2;
				if (Hulib.getInstance().get_hu_info(paiArray, 34, 34)) {
					paiArray[i] = paiArray[i] + 2;
					paiArray[place] = paiArray[place] + 1;
					return true;
				}
				paiArray[i] = paiArray[i] + 2;
			}
		}
		// 都不满足,加上手牌
		paiArray[place] = paiArray[place] + 1;
		return false;
	}

	/**
	 * 检测是否单调 不满足 返回0 ; 普通单吊 --1; 有红中 -- 2
	 * 
	 * @param p
	 * @param newList
	 *            手牌集合 已经加了pai的
	 * @param pai
	 * @param place
	 * @return
	 */
	public static Integer checkDanDiao(int[] paiArray, Integer[][] pai,
			int place) {
		Integer num = 0;
		Boolean hu = false;
		if (pai[0][0].equals(5)) {// 这张牌如果是红中,肯定是
			return 1;
		} else {// 检测普通单吊
			int place1 = place - 3;// 比它小于3的数的位置
			int place2 = place + 3;// 比它大于3的数的位置
			// 将手牌中胡的牌移除
			paiArray[place] = paiArray[place] - 1;
			if (pai[0][1] < 4) {
				// 查看大于3的数 3 4 5 6检测是不是吊 3,6
				num = paiArray[place2];
				if (num > 0) {
					paiArray[place2] = paiArray[place2] + 1;
					hu = Hulib.getInstance().get_hu_info(paiArray, 34, 34);
					if (hu) {// 说明不是单吊
						// 将原来修改的参数加上
						paiArray[place] = paiArray[place] + 1;
						paiArray[place2] = paiArray[place2] - 1;
						return 0;
					}
					paiArray[place2] = paiArray[place2] - 1;
				}
			} else if (pai[0][1] > 6) {
				// 检测小于3的数
				num = paiArray[place1];
				if (num > 0) {
					paiArray[place1] = paiArray[place1] + 1;
					hu = Hulib.getInstance().get_hu_info(paiArray, 34, 34);
					if (hu) {// 说明不是单吊
						paiArray[place] = paiArray[place] + 1;
						paiArray[place1] = paiArray[place1] - 1;
						return 0;
					}
					paiArray[place1] = paiArray[place1] - 1;
				}

			} else {// 4 5 6
				// 查看大于其3的数 检测是不是吊
				num = paiArray[place2];
				if (num > 0) {
					paiArray[place2] = paiArray[place2] + 1;
					hu = Hulib.getInstance().get_hu_info(paiArray, 34, 34);
					if (hu) {// 说明不是单吊
						// 将原来修改的参数加上
						paiArray[place] = paiArray[place] + 1;
						paiArray[place2] = paiArray[place2] - 1;
						return 0;
					}
					// 不胡也要还原
					paiArray[place2] = paiArray[place2] - 1;
				}
				// 检测小于其3的数 检测是否吊
				num = paiArray[place1];
				if (num > 0) {
					paiArray[place1] = paiArray[place1] + 1;
					hu = Hulib.getInstance().get_hu_info(paiArray, 34, 34);
					if (hu) {// 说明不是单吊
						paiArray[place] = paiArray[place] + 1;
						paiArray[place1] = paiArray[place1] - 1;
						return 0;
					}
					paiArray[place1] = paiArray[place1] - 1;
				}
			}
			// 都不胡，说明满足单吊
			paiArray[place] = paiArray[place] + 1;
			return 1;
		}
	}

	private static int findPlaceInArray(Integer[][] pai) {
		int type = pai[0][0];
		int paiNum = pai[0][1];
		int place = 0;
		if (type <= 4) {
			place = (type - 1) * 9 + paiNum - 1;
		} else {
			place = 30 + paiNum;// 3*9+4*1+paiNum-1
		}
		return place;
	}

	/**
	 * 检测是不是飘
	 * 
	 * @param p
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @return 1 是飘 0 不是飘
	 */
	private static Boolean isPiao(Player p, int[] paiArray) {
		// 如果有吃，不成立
		if (p.getChiList() != null && p.getChiList().size() > 0) {
			return false;
		}
		int twoNum = 0;// 手牌个数为2 的牌的个数
		for (int i = 0; i < paiArray.length; i++) {
			// 有4张一样的不成立
			if (paiArray[i] > 3) {// 有4个的不成立
				return false;
			} else if (paiArray[i] == 1) {// 如果个数为1,不成立
				return false;
			} else if (paiArray[i] == 2) {// 如果个数为2，首先默认为将,只能有一个将
				twoNum++;
			}
		}
		if (twoNum == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 检测是不是有七对
	 * 
	 * @param toUser
	 * @param winUser
	 * @param currentMjList
	 * @param pai
	 * @return
	 */
	public static boolean checkQiDui(int[] paiArray, int num) {
		if (num == 14) {
			for (int i : paiArray) {
				if (i % 2 == 1) {
					return false;
				}
			}
		} else if (num == 13) {
			int danzhang = 0;
			for (int i : paiArray) {
				if (i % 2 == 1) {
					danzhang++;
				}
			}
			if (danzhang != 1) {// 必须为1，混牌顶1个全是对子
				return false;
			}
		}
		return true;
	}

	/**
	 * 移除对子的方法
	 * 
	 * @param List
	 *            <Integer[][]> 手牌
	 * @return Integer 移除对子之后单张数
	 */
	public static Integer yichuDuizi(List<Integer[][]> newShouPaiList) {
		List<Integer[][]> allShouPaiList = new ArrayList<Integer[][]>(
				newShouPaiList.size());
		// 将数据保存到allShouPaiList 值传递，newShouPaiList改变和allShouPaiList没有影响
		allShouPaiList.addAll(newShouPaiList);
		paiXu(allShouPaiList);
		// [1,2,3,4,5,6] 取2-6角标
		for (int i = allShouPaiList.size() - 1; i >= 1; i--) {
			// [1,2,3,4,5,6] 取1-5 麻将中除了混牌，每样最多4张
			if (allShouPaiList.get(i)[0][0]
					.equals(allShouPaiList.get(i - 1)[0][0])
					&& allShouPaiList.get(i)[0][1].equals(allShouPaiList
							.get(i - 1)[0][1])) {
				allShouPaiList.remove(i);
				allShouPaiList.remove(i - 1);
				i--;
			}
		}
		// 没有对子，都是单牌
		return allShouPaiList.size();
	}

	/**
	 * 吃牌检测 如果玩家的牌只有四张，就不能吃！吃牌的时候，手里的牌必须大于4
	 * 
	 * @param p
	 * @param pai
	 */
	public static List<Integer[][]> checkChi(Player p, Integer[][] pai) {

		Integer[][] pai1 = new Integer[][] { { pai[0][0], pai[0][1] } };
		List<Integer[][]> result = new ArrayList<>();
		List<Integer[][]> sameType = new ArrayList<>();// 存放相同牌型的牌的集合

		if (pai[0][0] < 4) {// 万丙条
			// 存放相同牌型
			for (int i = 0; i < p.getCurrentMjList().size(); i++) {
				if (pai1[0][0].equals(p.getCurrentMjList().get(i)[0][0])) {
					boolean hasPai = false;
					for (int j = 0; j < sameType.size(); j++) {
						if (sameType.get(j)[0][0].equals(p.getCurrentMjList()
								.get(i)[0][0])
								&& sameType.get(j)[0][1].equals(p
										.getCurrentMjList().get(i)[0][1])) {
							hasPai = true;
						}
					}
					if (!hasPai) {
						sameType.add(new Integer[][] { {
								p.getCurrentMjList().get(i)[0][0],
								p.getCurrentMjList().get(i)[0][1] } });
					}
				}
			}
			// 检测能否吃
			// 如果，pai的值为1，要判断+1+2；
			// 如果，pai的值为9，要判断-1-2；
			// 如果，pai的值其他，要判断+1+2；-1-2；+1-1
			// 把所有牌型一致的牌两两组合，放入templete中，然后跟上述规则对比
			List<Integer[][]> templete = new ArrayList<>();
			for (int i = 0; i < sameType.size(); i++) {
				for (int j = i + 1; j < sameType.size(); j++) {
					templete.add(new Integer[][] {
							{ sameType.get(i)[0][0], sameType.get(i)[0][1] },
							{ sameType.get(j)[0][0], sameType.get(j)[0][1] } });
				}
			}
			for (int i = 0; i < templete.size(); i++) {
				if ((templete.get(i)[0][1].equals(pai1[0][1] + 1) && templete
						.get(i)[1][1].equals(pai1[0][1] + 2)) || // +1+2
						(templete.get(i)[0][1].equals(pai1[0][1] + 2) && templete
								.get(i)[1][1].equals(pai1[0][1] + 1)) || // +2+1
						(templete.get(i)[0][1].equals(pai1[0][1] - 1) && templete
								.get(i)[1][1].equals(pai1[0][1] - 2)) || // -1-2
						(templete.get(i)[0][1].equals(pai1[0][1] - 2) && templete
								.get(i)[1][1].equals(pai1[0][1] - 1)) || // -2-1
						(templete.get(i)[0][1].equals(pai1[0][1] - 1) && templete
								.get(i)[1][1].equals(pai1[0][1] + 1)) || // -1+1
						(templete.get(i)[0][1].equals(pai1[0][1] + 1) && templete
								.get(i)[1][1].equals(pai1[0][1] - 1))) {// +1-1
					result.add(templete.get(i));
				}
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 吃牌动作，传入玩家手上的牌，以及要吃的三张牌，返回吃牌后玩家的手上的牌
	 * 
	 * @param playerPais
	 * @param chiPais
	 * @return
	 */
	public static List<Integer[][]> chi(List<Integer[][]> playerPais,
			List<Integer[][]> chiPais) {
		if (playerPais.size() <= chiPais.size()) {
			return playerPais;
		}
		for (int i = 0; i < chiPais.size(); i++) {
			for (int j = 0; j < playerPais.size(); j++) {
				if (playerPais.get(j)[0][0].equals(chiPais.get(i)[0][0])
						&& playerPais.get(j)[0][1].equals(chiPais.get(i)[0][1])) {
					playerPais.remove(j);
					break;
				}
			}
		}
		return playerPais;
	}

	/**
	 * 检测能否杠 明杠(1点杠：手里3张和别人的一张size 为1;2碰杠：自己碰之后又摸了一张)size为3 暗杠（自摸4张）size 为4
	 * 
	 * @param p
	 * @param chuUser
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> checkGang(Player p, Player chuUser,
			Integer[][] pai) {
		List<Integer[][]> result = new ArrayList<>();
		List<Integer[][]> newList = p.getCurrentMjList();
		List<Integer[][]> currentMjList = getNewList(newList);
		if (p.getUserId().equals(chuUser.getUserId())) {// 自摸检测 --暗杠
			// 手中是否有四张
			if (pai != null) {
				currentMjList.add(new Integer[][] { { pai[0][0], pai[0][1] } });

			}
			paiXu(currentMjList);
			Set<String> set = new HashSet<>();
			for (int i = 0; i < currentMjList.size(); i++) {
				int num = 1;
				if (!set.contains(currentMjList.get(i)[0][0] + "_"
						+ currentMjList.get(i)[0][1])) {
					for (int j = i + 1; j < currentMjList.size(); j++) {// 从这张牌后面的一张检测
						if (currentMjList.get(i)[0][0].equals(currentMjList
								.get(j)[0][0])
								&& currentMjList.get(i)[0][1]
										.equals(currentMjList.get(j)[0][1])) {
							num++;
						}
					}
				}
				// 检测暗杠
				if (num == 4) {
					set.add(currentMjList.get(i)[0][0] + "_"
							+ currentMjList.get(i)[0][1]);
					result.add(new Integer[][] {
							{ currentMjList.get(i)[0][0],
									currentMjList.get(i)[0][1] },
							{ currentMjList.get(i)[0][0],
									currentMjList.get(i)[0][1] },
							{ currentMjList.get(i)[0][0],
									currentMjList.get(i)[0][1] },
							{ currentMjList.get(i)[0][0],
									currentMjList.get(i)[0][1] } });
				}
			}
			// 2能否和碰集合组合 --- 明杠
			// 需要检测手中的牌与碰牌集合有没有能组成杠的
			if (p.getPengList() != null && p.getPengList().size() > 0) {
				List<InfoCount> pengList = p.getPengList();
				for (InfoCount infoCount : pengList) {
					List<Integer[][]> list = infoCount.getL();
					for (int j = 0; j < currentMjList.size(); j++) {
						if (currentMjList.get(j)[0][0]
								.equals(list.get(0)[0][0])
								&& currentMjList.get(j)[0][1].equals(list
										.get(0)[0][1])) {
							result.add(new Integer[][] {
									{ currentMjList.get(j)[0][0],
											currentMjList.get(j)[0][1] },
									{ currentMjList.get(j)[0][0],
											currentMjList.get(j)[0][1] },
									{ currentMjList.get(j)[0][0],
											currentMjList.get(j)[0][1] } });
							break;
						}
					}
				}
			}
		} else {// 检测--明杠别人出牌，自己手里有3张.
			if (pai != null) {
				int num = 0;
				for (int j = 0; j < currentMjList.size(); j++) {
					if (pai[0][0].equals(currentMjList.get(j)[0][0])
							&& pai[0][1].equals(currentMjList.get(j)[0][1])) {
						num++;
						if (num == 3) {
							result.add(new Integer[][] { { pai[0][0], pai[0][1] } });
							break;
						}
					}
				}
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 
	 * 杠牌操作，传入玩家手中的牌，以及要杠的牌，返回杠的类型 --代改
	 * 
	 * @param p
	 * @param pais
	 *            size大小对应的值: 1点的杠（明杠） 返回4，3碰的杠（明杠），返回3 3暗杠size为4 返回5
	 */
	public static Integer gang(Player p, List<Integer[][]> pais) {
		Integer gangType = null;
		List<Integer[][]> currentMjList = p.getCurrentMjList();
		Integer[][] lastFaPai = p.getLastFaPai();
		Integer[][] pai = pais.get(0);
		if (pais.size() == 1) {// 1点杠
			removeNumPai(currentMjList, pais.get(0), 3);
			gangType = 4;
		} else if (pais.size() == 4) {// 自己暗杠
			if (lastFaPai != null
					&& (!lastFaPai[0][0].equals(pai[0][0]) || !lastFaPai[0][1]
							.equals(pai[0][1]))) {// 这张牌不是最后一张牌
				removeNumPai(currentMjList, pais.get(0), 4);
				currentMjList.add(lastFaPai);
				paiXu(currentMjList);
			} else {
				removeNumPai(currentMjList, pais.get(0), 3);// 移除3张牌 和发的那张牌
			}
			p.setLastFaPai(null);
			gangType = 5;
		} else if (pais.size() == 3) {// 自己的牌和自己的碰的牌组成的杠
			// 移除这个碰
			if (lastFaPai != null
					&& (!lastFaPai[0][0].equals(pai[0][0]) || !lastFaPai[0][1]
							.equals(pai[0][1]))) {// 这张牌不是最后一张牌
				removeNumPai(currentMjList, pai, 1);
				currentMjList.add(lastFaPai);
				paiXu(currentMjList);
			} else {
				removeNumPai(currentMjList, pai, 1);
			}
			p.setLastFaPai(null);
			InfoCount info = new InfoCount();
			List<InfoCount> pengList = p.getPengList();
			for (int i = pengList.size() - 1; i >= 0; i--) {
				List<Integer[][]> l = pengList.get(i).getL();
				if (l.get(0)[0][0].equals(pai[0][0])
						&& l.get(0)[0][1].equals(pai[0][1])) {
					info = pengList.get(i);
					pengList.remove(i);
					break;
				}
			}
			info.getL().add(new Integer[][] { { pai[0][0], pai[0][1] } });
			List<InfoCount> gangListType3 = p.getGangListType3();
			if (gangListType3 == null) {
				gangListType3 = new ArrayList<>();
			}
			gangListType3.add(info);
			gangType = 3;
		}
		return gangType;
	}

	/**
	 * 碰牌检测 如果玩家只有四张牌，需要检测是不是飘， 如果是飘，继续检测能不能碰，如果不是飘，就不能碰
	 * 
	 * @param p
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> checkPeng(Player p, Integer[][] pai) {
		// 这要检测手中这张牌有没有超过两张
		List<Integer[][]> result = new ArrayList<>();
		List<Integer[][]> currentMjList = p.getCurrentMjList();
		if (pai != null) {// 必须是别人出的牌
			int num = 0;
			for (Integer[][] integers : currentMjList) {
				if (integers[0][0].equals(pai[0][0])
						&& integers[0][1].equals(pai[0][1])) {
					num++;
				}
			}
			if (num >= 2) {
				result.add(new Integer[][] { { pai[0][0], pai[0][1] },
						{ pai[0][0], pai[0][1] } });
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 碰牌操作，传入玩家手中的牌，以及要碰的牌，返回碰之后的牌
	 * 
	 * @param playerPais
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> peng(List<Integer[][]> playerPais,
			Integer[][] pai) {
		return pengOrGang(playerPais, pai, 2);
	}

	/**
	 * 碰杠的执行方法
	 * 
	 * @param playerPais
	 * @param pai
	 * @param num
	 * @return
	 */
	private static List<Integer[][]> pengOrGang(List<Integer[][]> playerPais,
			Integer[][] pai, Integer num) {
		for (int n = 0; n < num; n++) {
			for (int i = playerPais.size() - 1; i >= 0; i--) {
				if (pai[0][0].equals(playerPais.get(i)[0][0])
						&& pai[0][1].equals(playerPais.get(i)[0][1])) {
					playerPais.remove(i);
					break;
				}
			}
		}
		return playerPais;
	}

	/**
	 * 移除n张牌
	 * 
	 * @param newList
	 *            牌集合
	 * @param integers
	 *            想要移除的牌
	 * @param i
	 *            想要移除的牌的个数
	 */
	public static void removeNumPai(List<Integer[][]> newList,
			Integer[][] integers, int i) {
		int num = 0;
		for (int j = newList.size() - 1; j >= 0; j--) {
			if (newList.get(j)[0][0].equals(integers[0][0])
					&& newList.get(j)[0][1].equals(integers[0][1])) {
				newList.remove(j);
				num++;
				if (num == i) {
					break;
				}
			}
		}
	}

	/**
	 * 获取碰的总番数
	 * 
	 * @param huUser
	 * @return
	 */
	public static Integer getpengFan(Player huUser) {
		Integer pengFan = 0;
		List<InfoCount> pengList = huUser.getPengList();
		for (InfoCount infoCount : pengList) {
			if (infoCount.getL().get(0)[0][0].equals(5)) {
				pengFan = Cnst.PENG_HONGZHONG_FAN;
			}
		}
		return pengFan;
	}

	/**
	 * 获取杠分
	 * 
	 * @param huUser
	 * @return
	 */
	public static Integer getGangFan(Player huUser) {
		Integer gangFan = 0;
		// 点杠
		List<InfoCount> gangListType4 = huUser.getGangListType4();
		// 碰杠
		List<InfoCount> gangListType3 = huUser.getGangListType3();
		// 暗杠
		List<InfoCount> gangListType5 = huUser.getGangListType5();
		if (gangListType4 != null && gangListType4.size() > 0) {
			for (InfoCount infoCount : gangListType4) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是红中
					gangFan += Cnst.GANG_HONGZHONG_MING;
				} else {
					gangFan += Cnst.GANG_TYPE_MING;
				}
			}
		}
		if (gangListType3 != null && gangListType3.size() > 0) {
			for (InfoCount infoCount : gangListType3) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是红中
					gangFan += Cnst.GANG_HONGZHONG_MING;
				} else {
					gangFan += Cnst.GANG_TYPE_MING;
				}
			}

		}
		if (gangListType5 != null && gangListType5.size() > 0) {
			for (InfoCount infoCount : gangListType5) {
				if (infoCount.getL().get(0)[0][0].equals(5)) {// 是红中
					gangFan += Cnst.GANG_HONGZHONG_AN;
				} else {
					gangFan += Cnst.GANG_TYPE_AN;
				}
			}
		}
		return gangFan;
	}

	/**
	 * 检测能否飘胡，在手牌剩下4张时检测
	 * 
	 * @param p
	 * @return
	 */
	public static boolean checkCanPiao(Player p) {
		List<InfoCount> chiList = p.getChiList();
		// 有吃 ,肯定不是飘
		if (chiList != null && chiList.size() != 0) {
			return false;
		}
		// //查看手牌是否满足飘 1 ：两个对子 2：1个三张，1个单张
		// List<Integer[][]> currentMjList = p.getCurrentMjList();
		// //也就是说不重复的牌不能大于两种
		// ArrayList<Integer[][]> distinct = getDistinct(currentMjList);
		// if(distinct.size()>2){
		// return false;
		// }
		return true;
	}

	public static Integer getLastPosition(Integer maxNum) {
		Integer lastPosition = 4;// 正常四人玩家为4
		if (maxNum == 2) {
			lastPosition = 3;// 东西 ,西风位置为3
		} else if (maxNum == 3) {
			lastPosition = 3;// 东南西 ,西风位置为3
		} else if (maxNum == 4) {
			lastPosition = 4;// 东南西北，被封位置为4
		}
		return lastPosition;
	}

	/**
	 * 检测是不是听
	 * 
	 * @param chuUser
	 * @return
	 */
	public static boolean checkTing(Player chuUser) {
		List<Integer[][]> currentMjList=chuUser.getCurrentMjList();
		//新的集合
		List<Integer[][]> newList = getNewList(currentMjList);
		int[] shouPaiArray = getShouPaiArray(newList, null);
		// 检测是不是七对 --七对的时候可以缺门，可以却1，9
		if (currentMjList.size() == 13) {
			if (checkQiDui(shouPaiArray,13)) {
				return true;
			}
		}
		int gui_index=0;
		for (int i=0;i<=shouPaiArray.length;i++) {
			if(shouPaiArray[i]==0){
				//这张牌就是混牌
				gui_index=i;
				//将其数量设置为1
				shouPaiArray[i]=1;
				break;
			}
		}
		boolean hu = Hulib.getInstance().get_hu_info(shouPaiArray, 34,gui_index);
		if(hu){
			shouPaiArray[gui_index]=0;
			Boolean hasChaOrGang = false;
			Boolean hasYiJiu = false;
			// list2用于存储所有的牌
			Set<Integer> paiNumList = new HashSet<Integer>();
			// 用于存储所有牌的大小（比如一万一并一条存1，5万5饼5条存5）
			Set<Integer> paiXingList = new HashSet<Integer>();
			//手牌中类型检测
			for (int i = 0; i < newList.size(); i++) {
				if (!newList.get(i)[0][0].equals(5)) {// 不加红中的牌数
					paiNumList.add(newList.get(i)[0][1]);
				}
				paiXingList.add(newList.get(i)[0][0]);
			}
			List<InfoCount> chiList = chuUser.getChiList();
			List<InfoCount> pengList = chuUser.getPengList();
			List<InfoCount> gangListType4 = chuUser.getGangListType4();
			List<InfoCount> gangListType3 = chuUser.getGangListType3();
			List<InfoCount> gangListType5 = chuUser.getGangListType5();
			// 首先获取吃碰杠的信息
			if (chiList != null && chiList.size() > 0) {
				for (InfoCount infoCount : chiList) {
					// 获取吃的类型
					paiXingList.add(infoCount.getL().get(0)[0][0]);
					// 获取吃的大小
					paiNumList.add(infoCount.getL().get(0)[0][1]);
					paiNumList.add(infoCount.getL().get(1)[0][1]);
					paiNumList.add(infoCount.getL().get(2)[0][1]);
				}
			}
			if (pengList != null && pengList.size() > 0) {
				hasChaOrGang = true;
				for (InfoCount infoCount : pengList) {
					paiXingList.add(infoCount.getL().get(0)[0][0]);
					paiNumList.add(infoCount.getL().get(0)[0][1]);
				}
			}
			if (gangListType4 != null && gangListType4.size() > 0) {
				hasChaOrGang = true;
				for (InfoCount infoCount : gangListType4) {
					paiXingList.add(infoCount.getL().get(0)[0][0]);
					paiNumList.add(infoCount.getL().get(0)[0][1]);
				}
			}
			if (gangListType3 != null && gangListType3.size() > 0) {
				hasChaOrGang = true;
				for (InfoCount infoCount : gangListType3) {
					paiXingList.add(infoCount.getL().get(0)[0][0]);
					paiNumList.add(infoCount.getL().get(0)[0][1]);
				}
			}
			if (gangListType5 != null && gangListType5.size() > 0) {
				hasChaOrGang = true;
				for (InfoCount infoCount : gangListType5) {
					paiXingList.add(infoCount.getL().get(0)[0][0]);
					paiNumList.add(infoCount.getL().get(0)[0][1]);
				}
			}
			// 检测1,9 牌型大小满足1或9或者类型满足5(红中可以顶1,9,可以免叉)
		
			if (paiNumList.contains(1) || paiNumList.contains(9)
					|| paiXingList.contains(5)) {
				hasYiJiu = true;
			}
			//如果上面没有叉，那么肯定没有红中
			if(paiXingList.contains(5)){
				hasChaOrGang=true;
			}
			paiXingList.remove(5);//移除红中
			//类型检测:要么有3色,要么清一色
			if(paiXingList.size()!=3 && paiXingList.size()!=1){
				return false;
			}
			//检测是否满足规则
			if(!hasYiJiu){//没有红中和1,9
				if(!hasYiJiu){//如果还没有，那就看看放入1,9 是否能胡
				   int[] x=getAllYiJiuIndex();
				   for (int i : x) {
					   //将其加入到此牌中检测是否胡
					   shouPaiArray[i]=1;
					   if(Hulib.getInstance().get_hu_info(shouPaiArray,34, 34)){//如果能胡
						   //查看是否有叉牌
						   if(checkChaInShouPai(shouPaiArray)){
							   return true;
						   }
						   //将其设置为0,继续检测
					   }
					   shouPaiArray[i]=0;
				   }
				}
			}else{//如果有1,9直接检测叉牌
				if(hasChaOrGang){//本身有叉牌
					return true;
				}else{//检测手牌中是否满足有叉牌
					//1：手牌中有大于等于3张的，移除之后，加上混可以胡
					//加如混牌
					shouPaiArray[gui_index]=1;
					for (int i=0;i<shouPaiArray.length;i++) {
						if(shouPaiArray[i]>=3){
							shouPaiArray[i]=shouPaiArray[i]-3;
							if(Hulib.getInstance().get_hu_info(shouPaiArray,34, gui_index)){
								return true;
							}
							shouPaiArray[i]=shouPaiArray[i]+3;
						}
					}
					//2：手牌中有两张的(混排顶一张,成为叉牌),移除之后可以胡
					//移除混牌
					shouPaiArray[gui_index]=0;
					for (int i=0;i<shouPaiArray.length;i++) {
						if(shouPaiArray[i]==2){
							shouPaiArray[i]=0;
							if(Hulib.getInstance().get_hu_info(shouPaiArray,34, 34)){
								return true;
							}
							shouPaiArray[i]=2;
						}
					}
				}
			}
		}
		//上面不满足
		return false;
	}

	private static int[] getAllYiJiuIndex() {
		int[] x = new int[6];
		int y = 0;
		for (int i = 0; i <= 26; i++) {
			if ((i + 1) % 9 == 1 || (i + 1) % 9 == 0) {
				x[y] = i;
				y++;
			}
		}
		return x;
	}

}
