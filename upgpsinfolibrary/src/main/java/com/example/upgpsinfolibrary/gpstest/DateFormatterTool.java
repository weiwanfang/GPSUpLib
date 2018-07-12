package com.example.upgpsinfolibrary.gpstest;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author WeiWanFang 2015-8-13 上午12:20:40
 * @description 时间日期格式工具
 */
public class DateFormatterTool {


    /**
     * 字符串转换为java.util.Date<br>
     * 支持格式为 yyyy.MM.dd G 'at' hh:mm:ss z 如 '2002-1-1 AD at 22:10:59 PSD'<br>
     * yy/MM/dd HH:mm:ss 如 '2002/1/1 17:55:00'<br>
     * yy/MM/dd HH:mm:ss pm 如 '2002/1/1 17:55:00 pm'<br>
     * yy-MM-dd HH:mm:ss 如 '2002-1-1 17:55:00' <br>
     * yy-MM-dd HH:mm:ss am 如 '2002-1-1 17:55:00 am' <br>
     *
     * @param time String 字符串<br>
     * @return Date 日期<br>
     */
    public static Date stringToDate(String time) {
        SimpleDateFormat formatter;
        int tempPos = time.indexOf("AD");
        time = time.trim();
        formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
        if (tempPos > -1) {
            time = time.substring(0, tempPos) + "公元"
                    + time.substring(tempPos + "AD".length());// china
            formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
        }
        tempPos = time.indexOf("-");
        if (tempPos > -1 && (time.indexOf(" ") < 0)) {
            formatter = new SimpleDateFormat("yyyyMMddHHmmssZ");
        } else if ((time.indexOf("/") > -1) && (time.indexOf(" ") > -1)) {
            formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        } else if ((time.indexOf("-") > -1) && (time.indexOf(" ") > -1)) {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else if ((time.indexOf("/") > -1) && (time.indexOf("am") > -1)
                || (time.indexOf("pm") > -1)) {
            formatter = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a");
        } else if ((time.indexOf("-") > -1) && (time.indexOf("am") > -1)
                || (time.indexOf("pm") > -1)) {
            formatter = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a");
        }
        ParsePosition pos = new ParsePosition(0);
        java.util.Date ctime = formatter.parse(time, pos);

        return ctime;
    }

    public static String formatDateTime(long mss) {
        String hourStr = null;
        String minutesStr = null;
        String secondsStr = null;
        String DateTimes = null;
        long days = mss / (60 * 60 * 24);
        long hours = (mss % (60 * 60 * 24)) / (60 * 60);
        long minutes = (mss % (60 * 60)) / 60;
        long seconds = mss % 60;
        if (days > 0) {
            DateTimes = days + "天" + hours + "小时" + minutes + "分钟"
                    + seconds + "秒";
        } else if (hours > 0) {
            hourStr = hours + "";
            minutesStr = minutes + "";
            secondsStr = seconds + "";
            if (hours < 10) {
                hourStr = "0" + hours;
            }
            if (minutes < 10) {
                minutesStr = "0" + minutes;
            }
            if (seconds < 10) {
                secondsStr = "0" + seconds;
            }
            DateTimes = hourStr + ":" + minutesStr + ":" + secondsStr + "";
        } else if (minutes > 0) {
            minutesStr = minutes + "";
            secondsStr = seconds + "";
            if (minutes < 10) {
                minutesStr = "0" + minutes;
            }
            if (seconds < 10) {
                secondsStr = "0" + seconds;
            }
            DateTimes = minutesStr + ":" + secondsStr + "";
        } else {
            secondsStr = seconds + "";
            if (seconds < 10) {
                secondsStr = "0" + seconds;
            }
            DateTimes = "00:" + secondsStr + "";
        }
        return DateTimes;
    }

    /**
     * 将java.util.Date 格式转换为字符串格式'yyyy-MM-dd HH:mm:ss'(24小时制)<br>
     * 如Sat May 11 17:24:21 CST 2002 to '2002-05-11 17:24:21'<br>
     *
     * @param time Date 日期<br>
     * @return String 字符串<br>
     */

    public static String dateToString(Date time) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("HH:mm:ss");
        String ctime = formatter.format(time);
        return ctime;
    }


    /**
     * 将java.util.Date 格式转换为字符串格式'yyyy-MM-dd HH:mm:ss'(24小时制)<br>
     * 如Sat May 11 17:24:21 CST 2002 to '2002-05-11 17:24:21'<br>
     *
     * @param time Date 日期<br>
     * @return String 字符串<br>
     */

    public static String dateToString(String time) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = stringToDate(time);
        String ctime = formatter.format(date);
        return ctime;
    }


    /**
     * 将java.util.Date 格式转换为字符串格式'yyyy-MM-dd HH:mm:ss a'(12小时制)<br>
     * 如Sat May 11 17:23:22 CST 2002 to '2002-05-11 05:23:22 下午'<br>
     *
     * @param time Date 日期<br>
     * @param x    int 任意整数如：1<br>
     * @return String 字符串<br>
     */
    public static String dateToString(Date time, int x) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss");
        String ctime = formatter.format(time);
        return ctime;
    }

    /**
     * 取系统当前时间:返回只值为如下形式 2002-10-30 20:24:39
     *
     * @return String
     */
    public static String getNow() {
        return dateToString(new Date());
    }

    /**
     * 取系统当前时间:返回只值为如下形式 2002-10-30 08:28:56 下午
     *
     * @param hour 为任意整数
     * @return String
     */
    public static String Now(int hour) {
        return dateToString(new Date(), hour);
    }

    /**
     * 取系统当前时间:返回值为如下形式 2002-10-30
     *
     * @return String
     */
    public static String getYYYY_MM_DD() {
        return dateToString(new Date()).substring(0, 10);

    }

    /**
     * 取系统给定时间:返回值为如下形式 2002-10-30
     *
     * @return String
     */
    public static String getYYYY_MM_DD(String date) {
        return date.substring(0, 10);

    }


    public static String changeDate(String dateStr) {
        String string = null;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            Date d = sf.parse(dateStr);
            System.out.println(d);
            SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            string = sf1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return string;
    }


    public static String changeDate22(String dateStr) {
        String string = null;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            Date d = sf.parse(dateStr);
            System.out.println(d);
            SimpleDateFormat sf1 = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            string = sf1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String getHour() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("H");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    public static String getDay() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("d");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    public static String getMonth() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("M");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    public static String getYear() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    public static String getWeek() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("E");
        String ctime = formatter.format(new Date());
        return ctime;
    }

    /*将字符串转为时间戳*/
    public static long getStringToDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date();
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }


    public static String getTime() {
        long time = System.currentTimeMillis() / 1000;//获取系统时间的10位的时间戳
        String str = String.valueOf(time);
        return str;

    }


    public static String friendlyTimeFormat(long endTimeL, long startTimeL) {
        String ftime = "";
        Date endTime = new Date((endTimeL * 1000));
        if (endTime == null) {
            return "";
        }
        Date startTime = new Date((startTimeL * 1000));
        if (startTime == null) {
            return "";
        }
        ftime = Math.max((endTime.getTime() - startTime.getTime()) / 1000, 1) + "";//单位为秒
        return ftime;
    }


    /**
     * 字符串的日期格式的计算
     */
    public static String daysBetween(String smdate, String bdate) throws ParseException {
        String dateStr = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(smdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(bdate));
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        // 事项开始日期(今天、昨天、前天或日期)
        int dateNum = Integer.parseInt(String.valueOf(between_days));
        if (dateNum == 0) {
            dateStr = "今天";
        } else if (dateNum == 1) {
            dateStr = "昨天";
        } else if (dateNum == 2) {
            dateStr = "前天";
        } else {
            dateStr = stringPattern(smdate, "yyyy-MM-dd HH:mm", "MM-dd");
        }

        return dateStr;
    }


    /**
     * 将String型格式化,比如想要将2011-11-11格式化成2011年11月11日,就StringPattern("2011-11-11",
     * "yyyy-MM-dd","yyyy年MM月dd日").
     *
     * @param date       String 想要格式化的日期
     * @param oldPattern String 想要格式化的日期的现有格式
     * @param newPattern String 想要格式化成什么格式
     * @return String
     */
    public static String stringPattern(String date, String oldPattern, String newPattern) {
        if (date == null || oldPattern == null || newPattern == null)
            return "";
        SimpleDateFormat sdf1 = new SimpleDateFormat(oldPattern); // 实例化模板对象
        SimpleDateFormat sdf2 = new SimpleDateFormat(newPattern); // 实例化模板对象
        Date d = null;
        try {
            d = sdf1.parse(date); // 将给定的字符串中的日期提取出来
        } catch (Exception e) { // 如果提供的字符串格式有错误，则进行异常处理
            e.printStackTrace(); // 打印异常信息
        }
        return sdf2.format(d);
    }

    private static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    /*时间戳转换成字符窜(将PHP时间戳转为java)*/
    public static String getDateToString(long time) {
        Date d = new Date((time * 1000));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }


    /*时间戳转换成字符窜(将PHP时间戳转为java)*/
    public static String getDateToStringMMddDHHmm(long time) {
        Date d = new Date((time * 1000));
        SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm");
        return sf.format(d);
    }

    /**
     * 将字符串转位日期类型
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return sdf.parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }


    public static String friendlyTimeFormat(long timeLong) {
        Date time = new Date((timeLong * 1000));
        if (time == null) {
            return "";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();
        //判断是否是同一天
        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }
        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        }
// else if(days > 2 && days <= 10){
// ftime = days+"天前";
// }
        else if (days > 2 && days <= 30) {
            ftime = days + "天前";
        } else if (days > 30 && days <= 60) {
            ftime = "1个月前";
        } else if (days > 60 && days <= 90) {
            ftime = "2个月前";
        } else if (days > 90 && days <= 120) {
            ftime = "3个月前";
        } else if (days > 120 && days <= 150) {
            ftime = "4个月前";
        } else if (days > 150 && days <= 180) {
            ftime = "5个月前";
        } else if (days > 180 && days <= 210) {
            ftime = "6个月前";
        } else if (days > 210 && days <= 240) {
            ftime = "7个月前";
        } else if (days > 240 && days <= 270) {
            ftime = "8个月前";
        } else if (days > 270 && days <= 300) {
            ftime = "9个月前";
        } else if (days > 300 && days <= 330) {
            ftime = "10个月前";
        } else if (days > 330 && days <= 360) {
            ftime = "11个月前";
        } else if (days > 360 && days <= 720) {
            ftime = "一年前";
        } else if (days > 720 && days <= 1080) {
            ftime = "两年前";
        } else if (days > 1080) {
            ftime = dateFormater2.get().format(time);
        }
        return ftime;
    }

    //    expirationTime, nowTime
    public static int compareTime(String dateString_01, String dateString_02) {
        int result = -5;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//        String dateString_01 = "2016-01-01 11:11:11";
//        String dateString_02 = "2016-01-02 11:11:11";
        try {
            Date date_01 = sdf.parse(dateString_01);
            Date date_02 = sdf.parse(dateString_02);
//            MDebug.debug("推荐-下拉刷新--json-time-", date_02.compareTo(date_01));
//            System.out.println("时间比较---" + date_01.before(date_02)); //true，当 date_01 小于 date_02 时，为 true，否则为 false
//            System.out.println("时间比较---" + date_02.after(date_01)); //true，当 date_02 大于 date_01 时，为 true，否则为 false
//            System.out.println("时间比较---" + date_01.compareTo(date_02)); //-1，当 date_01 小于 date_02 时，为 -1
//            System.out.println("时间比较---" + date_02.compareTo(date_01)); //1，当 date_02 小于 date_01 时，为1,没有过期
            result = date_02.compareTo(date_01);//等于1 则过期了
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else if (nowTime.compareTo(beginTime) == 0 || nowTime.compareTo(endTime) == 0) {
            return true;
        } else {
            return false;
        }
    }


}
