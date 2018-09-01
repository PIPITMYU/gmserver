package com.leo.rms.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * 
 *@company：ktmt
 *@package com.meif.rosis.utils
 *@title 文件名：DateUtil.java
 *@author (作者): zhoucong 
 *@date 日期：2014年1月6日下午1:34:11
 *@version (版本信息)： Copyright 2014 版权所有
 */
public class DateUtil {
	
	
	/**
	 * 将DATE类型转成string型
	 * @param date 日期
	 * @param format 日期格式（Constants中有详细定义）
	 * @return
	 * String
	 */
	public static String dateFormatString(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String dString = sdf.format(date);
		return dString;
	}
	
	
	/** 
	  * 将短时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	  * 
	  * @param strDate 
	  * @return 
	  */
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}
	
	/**	
	 * 将String格式时间转换为Date类型时间，传入strDate格式为：yyyy-MM-dd,
	 * 	返回的时间格式也为：yyyy-MM-dd
	 * @param strDate
	 * @return
	 * @throws Exception   
	*/
	public static Date strDateToDate(String strDate)throws Exception{
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = localSimpleDateFormat.parse(strDate);
		return date1;
	}
	
	/**	
	 * 计算时间差，单位：天
	 * 注意事项：   时间格式为：YYYY-MM-DD
	 * @param begDate
	 * @param endDate
	 * @return   
	*/
	public static int dateDifference(Date begDate,Date endDate){
		int result = 0;
		long sub = endDate.getTime() - begDate.getTime();
		long day = sub/(24*60*60*1000);
		result = (int)(day + 1);
		return result;
	}

	/**
	 * 获得当前时间，格式yyyy-MM-dd hh:mm:ss
	 * @param format
	 * @return
	 */
	public static String getCurrentDate() {
		return getCurrentDate("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 获得当前时间，格式自定义
	 * @param format
	 * @return
	 */
	public static String getCurrentDate(String format) {
		Calendar day = Calendar.getInstance();
		day.add(Calendar.DATE, 0);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(day.getTime());
		return date;
	}

	/**
	 * 获得昨天时间，格式自定义
	 * @param format
	 * @return
	 */
	public static String getYesterdayDate(String format) {
		Calendar day = Calendar.getInstance();
		day.add(Calendar.DATE, -1);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(day.getTime());
		return date;
	}

	/**  
	 * @param date1 需要比较的时间 不能为空(null),需要正确的日期格式 ,如：2009-09-12 
	 * @param date2 被比较的时间  为空(null)则为当前时间  
	 * @param stype 返回值类型   0为多少天，1为多少个月，2为多少年  
	 * @return  
	 * 举例：
	 * compareDate("2009-09-12", null, 0);//比较天
	 * compareDate("2009-09-12", null, 1);//比较月
	 * compareDate("2009-09-12", null, 2);//比较年
	 */
	@SuppressWarnings("all")
	public static int compareDate(String startDay, String endDay, int stype) {
		int n = 0;
		String[] u = { "天", "月", "年" };
		String formatStyle = stype == 1 ? "yyyy-MM" : "yyyy-MM-dd";

		endDay = endDay == null ? getCurrentDate("yyyy-MM-dd") : endDay;

		DateFormat df = new SimpleDateFormat(formatStyle);
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			c1.setTime(df.parse(startDay));
			c2.setTime(df.parse(endDay));
		} catch (Exception e3) {
			System.out.println("wrong occured");
		}
		while (!c1.after(c2)) { // 循环对比，直到相等，n 就是所要的结果  
			n++;
			if (stype == 1) {
				c1.add(Calendar.MONTH, 1); // 比较月份，月份+1  
			} else {
				c1.add(Calendar.DATE, 1); // 比较天数，日期+1  
			}
		}
		n = n - 1;
		if (stype == 2) {
			n = n / 365;
		}
		return n;
	}
	
	/**
	 * 比较两个日期，返回一个多少年多少月多少天
	 * @param startDate
	 * @param endDate
	 * @return
	 * String
	 */
	public static String compareDate(String startDate,String endDate){
		String compareDate = "";
		int day = compareDate(startDate,endDate,0);
		int year = day / 365;
		if(year !=0){
			compareDate += String.valueOf(year)+"年";
		}
		int residue = day % 365;
		if(residue!=0){
			int month = residue / 12;
			if(month!=0){
				compareDate += String.valueOf(month)+"个月";			
			}
			int residue2 = residue % 12;
			if(residue2!=0){
				compareDate += String.valueOf(residue2)+"天";
			}
		}
		if(compareDate==null || "".equals(compareDate)){
			compareDate+="新手报到";
		}
		
		String cd = compareDate.toString();
		return cd;
	}

	/**
	 * 判断时间是否符合时间格式
	 */
	public static boolean isDate(String date, String dateFormat) {
		if (date != null) {
			java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(dateFormat);
			format.setLenient(false);
			try {
				format.format(format.parse(date));
			} catch (ParseException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 实现给定某日期，判断是星期几
	 * date:必须yyyy-MM-dd格式
	 */
	public static String getWeekday(String date) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdw = new SimpleDateFormat("E");
		Date d = null;
		try {
			d = sd.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdw.format(d);
	}

	/**
	 * 用来全局控制 上一周，本周，下一周的周数变化
	 */
	private static int weeks = 0;

	/**
	 * 获得当前日期与本周一相差的天数
	 */
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// 获得今天是一周的第几天，星期一是第一天，星期二是第二天......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			return -6;
		} else {
			return 2 - dayOfWeek;
		}
	}

	/**
	 * 获得本周星期一的日期
	 */
	public static String getCurrentMonday(String format) {
		weeks = 0;
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(Calendar.DATE, mondayPlus);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(currentDate.getTime());
		return date;
	}

	/**
	 * 获得上周星期一的日期
	 */
	public static String getPreviousMonday(String format) {
		weeks--;
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(currentDate.getTime());
		return date;
	}

	/**
	 * 获得下周星期一的日期
	 */
	public static String getNextMonday(String format) {
		weeks++;
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(currentDate.getTime());
		return date;
	}

	/**
	 * 获得相应周的周日的日期
	 * 此方法必须写在getCurrentMonday，getPreviousMonday或getNextMonday方法之后
	 */
	public static String getSunday(String format) {
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(Calendar.DATE, mondayPlus + 7 * weeks + 6);
		SimpleDateFormat sdf = new SimpleDateFormat(format);//"yyyy-MM-dd"
		String date = sdf.format(currentDate.getTime());
		return date;
	}

	/**
	 * method 将字符串类型的日期转换为一个timestamp（时间戳记java.sql.Timestamp）
	 * @param dateString 需要转换为timestamp的字符串
	 * @return dataTime timestamp
	 */
	public final static java.sql.Timestamp string2Time(String dateString) {
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);// 设定格式
		dateFormat.setLenient(false);
		java.util.Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new java.sql.Timestamp(date.getTime());// Timestamp类型,timeDate.getTime()返回一个long型
	}

	/**
	 *@method 将字符串类型的日期转换为一个Date（java.sql.Date）
	 *@param dateString  需要转换为Date的字符串
	 *@return dataTime Date
	 */
	public final static java.sql.Date string2Date(String dateString) {
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		dateFormat.setLenient(false);
		java.util.Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new java.sql.Date(date.getTime());
	}

	//记录考勤， 记录迟到、早退时间
	public static String getState() {
		String state = "正常";
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		Date d = new Date();
		try {
			Date d1 = df.parse("08:00:00");
			Date d2 = df.parse(df.format(d));
			Date d3 = df.parse("18:00:00");

			int t1 = (int) d1.getTime();
			int t2 = (int) d2.getTime();
			int t3 = (int) d3.getTime();
			if (t2 < t1) {

				long between = (t1 - t2) / 1000;// 除以1000是为了转换成秒   
				long hour1 = between % (24 * 3600) / 3600;
				long minute1 = between % 3600 / 60;

				state = "迟到 ：" + hour1 + "时" + minute1 + "分";

			} else if (t2 < t3) {
				long between = (t3 - t2) / 1000;// 除以1000是为了转换成秒   
				long hour1 = between % (24 * 3600) / 3600;
				long minute1 = between % 3600 / 60;
				state = "早退 ：" + hour1 + "时" + minute1 + "分";
			}
			return state;
		} catch (Exception e) {
			return state;
		}

	}

	/**
	 * 两个时间相差距离多少天多少小时多少分多少秒
	 * @param str1 时间参数 1 格式：1990-01-01 12:00:00
	 * @param str2 时间参数 2 格式：2009-01-01 12:00:00
	 * @return String 返回值为：xx天xx小时xx分xx秒；
	 *                如果没有天，则是当天+时间；
	 *                如美没有小时则是多少分钟；
	 *                如果没有分钟，则是多少秒
	 */
	public static String getDistanceTime(String str1, String str2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date one;
		Date two;
		
		String day1 = str1.substring(8, 10);
		String day2 = str2.substring(8, 10);
		if(Integer.parseInt(day2)-Integer.parseInt(day1)>0){
			return str1;
		}else{		
			long day = 0;
			long hour = 0;
			long min = 0;
			long sec = 0;
			try {
				one = df.parse(str1);
				two = df.parse(str2);
				long time1 = one.getTime();
				long time2 = two.getTime();
				long diff;
				if (time1 < time2) {
					diff = time2 - time1;
				} else {
					diff = time1 - time2;
				}
				day = diff / (24 * 60 * 60 * 1000);
				hour = (diff / (60 * 60 * 1000) - day * 24);
				min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
				sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if (day != 0 || hour != 0) {//如果相隔时间超过小时，则返回完整的时间
				if (day != 0) {
					return str1;
				} else {
					return "今天" + str1.substring(10);
				}
			} else {
				if (min != 0) {//如果相差分钟，则返回相隔的分钟
					return min + "分钟之前";
				} else {//如果相差秒数，则返回相隔的秒数
					return sec + "秒之前";
				}
			}
		}
	}

	/** 
	  * 得到一个时间延后或前移几天的时间,nowdate为时间,delay为前移或后延的天数 
	  */
	public static String getNextDay(String nowdate, String delay) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String mdate = "";
			Date d = strToDate(nowdate);
			long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
			d.setTime(myTime * 1000);
			mdate = format.format(d);
			return mdate;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 判断时间点位置
	 * @param firstDate
	 * @param lastDate
	 * @return 0-开始之前 1-之间 2-结束之后 3-异常
	 */
	public static int isBetweenParams(String firstDate, String lastDate) {
		if (("".equals(firstDate) || firstDate == null) && ("".equals(lastDate) || lastDate == null)) {
			return 3;
		}
		Date beforeDate = strToDate(firstDate);
		Date afterDate = strToDate(lastDate);
		Date nowDate = new Date();
		if (nowDate.after(beforeDate) && nowDate.before(afterDate)) {
			return 1;
		} else if (nowDate.before(beforeDate)) {
			return 0;
		} else if (nowDate.after(afterDate)) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	  * 判断是否润年
	  * 
	  * @param ddate
	  * @return
	  */
	 public static boolean isLeapYear(String ddate) { 
	  /**
	   * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
	   * 3.能被4整除同时能被100整除则不是闰年
	   */
		  Date d = strToDate(ddate);
		  GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		  gc.setTime(d);
		  int year = gc.get(Calendar.YEAR);
		  if ((year % 400) == 0)
			  return true;
		  else if ((year % 4) == 0) {
			  if ((year % 100) == 0)
				  return false;
			  else
				  return true;
		  } else
			  return false;
	 } 
	 
	 /**
	  * 获取一个月的最后一天
	  * 
	  * @param dat
	  * @return
	  */
	 public static String getEndDateOfMonth(String dat) {// yyyy-MM-dd
		 String str = dat.substring(0, 8);
		 String month = dat.substring(5, 7);
		 int mon = Integer.parseInt(month);
		 if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8 || mon == 10 || mon == 12) {
			 str += "31";
		 } else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
			 str += "30";
		 } else {
			 if (isLeapYear(dat)) {
				 str += "29";
			 } else {
				 str += "28";
			 }
		 }
		 return str;
	 } 
	 
	 /**
	  * 判断二个时间是否在同一个周 
	  * @param date1
	  * @param date2
	  * @return
	  */
	 public static boolean isSameWeekDates(Date date1, Date date2) {
		 Calendar cal1 = Calendar.getInstance();
		 Calendar cal2 = Calendar.getInstance();
		 cal1.setTime(date1);
		 cal2.setTime(date2);
		 int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		 if (0 == subYear) {
			 if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				 return true;
		 } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
			 // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
			 if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				 return true;
		 } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
			 if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				 return true;
		 }
		 return false;
	 } 

	 /**
	  * 产生周序列,即得到当前时间所在的年度是第几周
	  * 
	  * @return
	  */
	 public static String getSeqWeek() {
		 Calendar c = Calendar.getInstance(Locale.CHINA);
		 String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
		 if (week.length() == 1)
			 week = "0" + week;
		 String year = Integer.toString(c.get(Calendar.YEAR));
		 return year + week;
	 } 
	
	 
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//        String s = getState();   
		//        System.out.println(s);
		//        
		//        String x = getDistanceTime("2011-11-25 11:00:36","2011-11-25 14:01:54");
		//        System.out.println(x);

		//        Date date1 = new Date();
		//        String dateString1 = dateFormatString(date1,"yyyyMMddHHmmss");
		//        double dateInt1 = Double.parseDouble(dateString1);
		//        System.out.println(dateFormatString(new Date(),"yyyyMMddhhmmssSSS"));Math.random()*100000
		//        System.out.println(dateFormatString(new Date(),"yyyyMMddhhmmssSSS"));
		//		  for(int i=0;i<=1000;i++){
		//			  Double doubleTest = Math.random()*10000000;
		//			  String str = doubleTest.toString();
		//			  System.out.println(str.substring(0, 5));
		//		  }
		//        Date date2 = new Date();
		//        String dateString2 = dateFormatString(date2,"yyyyMMddHHmmss");
		//        double dateInt2 = Double.parseDouble(dateString2);
		//        
		//        if(dateInt1<dateInt2){
		//        	System.out.println("++++++++++++++++++++++++++++++++++++");
		//        }else{
		//        	System.out.println("------------------------------------");
		//        }
		//        System.out.println(dateFormatString(date,"yyyy-MM-dd HH:mm:ss"));
		//        System.out.println(dateFormatString(date,"yyyy-MM-dd hh:mm:ss"));
		//        System.out.println(dateFormatString(date,"yyyy-MM-dd"));
		//        System.out.println(dateFormatString(date,"HH:mm:ss"));
		//        System.out.println(dateFormatString(date,"yyyy/MM/dd HH:mm:ss"));
		//        System.out.println(dateFormatString(date,"yyyy年MM月dd日 HH时mm分ss秒"));
		//        System.out.println(dateFormatString(date,"yyyy年MM月dd日 HH时mm分ss秒"));
		//        System.out.println(dateFormatString(date,"yyyy年MM月dd日"));
		//        System.out.println(dateFormatString(date,Constants.TIME_TYPE3));
		//        System.out.println(dateFormatString(date,"HH时"));

		//		for (int i = 0; i < 1000; i++) {
		//			System.out.println(StringUtils.getFileName());
		//		}

		//System.out.println(isBetweenParams("2012-03-17 10:00:00", "2012-03-18 10:00:00"));
		//System.out.println(getWeekday("2012-03-25"));
		//System.out.println(compareDate("2009-11-10","2012-04-13"));
		System.out.println(getWeekOfDate("2015/06/03" ) );
	}
	
	public static String getWeekOfDate(String date) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat sdw = new SimpleDateFormat("E");
		Date d = null;
		try {
			d = sd.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdw.format(d);
	}

}
