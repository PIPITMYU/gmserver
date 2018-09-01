package com.up72.game.utils.wx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.up72.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 微信业务工具类，对应商户用户
 */
public class WxGameUtil {

    public static String appId; // 应用ID
    public static String appSecret; // 应用密钥
    public static String originalId; // 原始ID
    public static String autoLoginPageAuthorizeRedirect;//自动登录页面授权回调地址

    private static Logger logger = LoggerFactory.getLogger(WxGameUtil.class);

    static {
        loadProperty(); // 加载配置文件
    }

    // 加载配置文件
    public static void loadProperty() {
        Properties properties = getProperties();
        appId = properties.getProperty("appId");
        appSecret = properties.getProperty("appSecret");
        originalId = properties.getProperty("originalId");
        autoLoginPageAuthorizeRedirect = properties.getProperty("autoLoginPageAuthorizeRedirect");
        if (appId == null || appSecret == null || originalId == null || autoLoginPageAuthorizeRedirect == null) {
            logger.error("获取微信配置出错");
        }
    }

    // 获取配置文件
    private static Properties getProperties() {
        InputStream inputStream = null;
        Properties properties = null;
        try {
            inputStream = WxGameUtil.class.getResourceAsStream("/wx.properties");
            if (inputStream == null) {
                logger.error("配置文件不存在：wx.properties");
            }
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            logger.error("加载配置文件出错", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (properties == null) {
            logger.error("初始化微信配置失败");
        }
        return properties;
    }

    // 发送文本消息
    private static final String MESSAGE_SEND = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";
    // 页面授权
    private static final String AUTH_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?grant_type=authorization_code&code=";
    // 发送模板消息
    private static final String TEMPLATE_MSG_SEND = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    // 获取用户信息
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?lang=zh_CN&access_token=";
    // 网页授权--获取用户信息
    private static final String PAGE_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?lang=zh_CN&access_token=";
    // 出发页面授权URL
    private static final String PAGE_AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?";

    public static String getAutoLoginAuthorizePath() {
        String path = PAGE_AUTHORIZE_URL + "appid=" + appId + "&redirect_uri=" + URLEncoder.encode("http://www.yiweimall.cn/jsp/wx/index.jsp") + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
        return path;
    }

    public static String getCustomLoginAuthorizePath(String page) {
        String path = PAGE_AUTHORIZE_URL + "appid=" + appId + "&redirect_uri=" + URLEncoder.encode(page) + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
        return path;
    }

    /**
     * 获取微信用户openId
     *
     * @param code 网页授权code
     * @return
     */
    public static String getOpenId(String code) {
        String openid = "";
        try {
            String url = AUTH_ACCESS_TOKEN + code + "&appid=" + appId + "&secret=" + appSecret;
            String str = WxUtil.sendHttpGet(url);
            JSONObject obj = JSONObject.parseObject(str);
            openid = obj.get("openid") == null ? "" : (String) obj.get("openid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return openid;
    }

    /**
     * 获取网页授权
     *
     * @param code 网页授权code
     * @return
     */
    public static String getAccess_tokenByPage(String code) {
        String openid = "";
        try {
            String url = AUTH_ACCESS_TOKEN + code + "&appid=" + appId + "&secret=" + appSecret;
            String str = WxUtil.sendHttpGet(url);
            JSONObject obj = JSONObject.parseObject(str);
            openid = obj.get("access_token") == null ? "" : (String) obj.get("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return openid;
    }


    /**
     * 获取用户信息
     *
     * @param openId 微信用户openId
     * @return
     */
    public static WxUserInfoDto getWxUserInfo(String openId) {
        WxUserInfoDto wxUserInfoDto = null;
        try {
            String url = USER_INFO_URL + URLEncoder.encode(WxUtil.getAccessToken(appId, appSecret), "UTF-8") + "&openid=" + openId;
            logger.warn("微信获取用户信息路径----------：" + url);
            //发送
            String str = WxUtil.sendHttpGet(url);
            JSONObject obj = JSONObject.parseObject(str);
            logger.warn("微信获取用户信息----------：" + str);
            Object errcode = obj.get("errcode");
            if (errcode == null) {
                wxUserInfoDto = (WxUserInfoDto) JSON.parseObject(JSON.toJSONString(obj), WxUserInfoDto.class);
            } else {
                int code = obj.getInteger("errcode");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wxUserInfoDto;
    }

    /**
     * 网页授权--获取用户信息
     *
     * @param code 网页码
     * @return
     */
    public static WxUserInfoDto getWxUserInfoByPage(String code) {
        WxUserInfoDto wxUserInfoDto = null;
        try {
            logger.warn("appid====" + appId);
            logger.warn("secret====" + appSecret);
            String access_tokenUrl = AUTH_ACCESS_TOKEN + code + "&appid=" + appId + "&secret=" + appSecret;
            logger.warn("网页授权-微信获取用户access_tokenUrl:" + access_tokenUrl);
            String access_tokenstr = WxUtil.sendHttpGet(access_tokenUrl);
            logger.warn("网页授权-微信获取用户access_tokenstr:" + access_tokenstr);
            JSONObject tokenObj = JSONObject.parseObject(access_tokenstr);
            logger.warn("网页授权-微信获取用户tokenObj:" + tokenObj);
            String openid = tokenObj.get("openid") == null ? "" : (String) tokenObj.get("openid");
            logger.warn("网页授权-微信获取用户openid:" + openid);
            String access_token = tokenObj.get("access_token") == null ? "" : (String) tokenObj.get("access_token");
            logger.warn("网页授权-微信获取用户access_token:" + access_token);
            String url = PAGE_USER_INFO_URL + access_token + "&openid=" + openid;
            //发送
            String str = WxUtil.sendHttpGet(url);
            JSONObject obj = JSONObject.parseObject(str);
            logger.warn("网页授权-微信获取用户信息----------：" + str);
            Object errcode = obj.get("errcode");
            if (errcode == null) {
                wxUserInfoDto = (WxUserInfoDto) JSON.parseObject(JSON.toJSONString(obj), WxUserInfoDto.class);
            } else {
                int returnCode = obj.getInteger("errcode");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wxUserInfoDto;
    }


    /**
     * 发送模板消息
     *
     * @param openId     接收用户openId
     * @param templateId 模板消息id
     * @param url        点击消息跳转页面地址（外网地址）
     * @param msgMap     根据模板消息格式构造，key参数名，value值；
     * @return 0发送成功，10000系统异常，其他为微信错误代码
     */
    public static int sendTemplateMsg(String openId, String templateId, String url, Map<String, String> msgMap) {
        int code = 0;
        try {
            String sendUrl = TEMPLATE_MSG_SEND + URLEncoder.encode(WxUtil.getAccessToken(appId, appSecret), "UTF-8");
            Map<String, Object> map = new HashMap<String, Object>(); //发送格式
            map.put("touser", openId);
            map.put("template_id", templateId);
            map.put("url", url);
            map.put("topcolor", "#FF0000");
            Map<String, Object> data = new HashMap<String, Object>();

            for (Map.Entry<String, String> entry : msgMap.entrySet()) {
                Map<String, Object> keynote = new HashMap<String, Object>();
                keynote.put("value", entry.getValue());
                keynote.put("color", "#173177");
                data.put(entry.getKey(), keynote);
            }

            map.put("data", data);
            String jsonStr = WxUtil.instants().sendHttpPost(sendUrl, JsonUtil.object2Json(map));
            JSONObject obj = JSONObject.parseObject(jsonStr);
            code = obj.getInteger("errcode");
        } catch (Exception e) {
            e.printStackTrace();
            code = 10000;
        }
        return code;
    }

    /**
     * 发送微信消息； 注：48小时内与公众号有互动的能收到；
     *
     * @param content 消息内容
     * @param openId  用户opendId
     */
    public static void sendWxMsgToUser(String content, String openId) {
        try {
            String url = MESSAGE_SEND + URLEncoder.encode(WxUtil.getAccessToken(appId, appSecret), "UTF-8");
            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> text = new HashMap<String, Object>();
            content = dealTextContent(content);
            text.put("content", content);
            map.put("text", text);
            map.put("msgtype", "text");
            map.put("touser", openId);
            String jsonStr = WxUtil.instants().sendHttpPost(url, JsonUtil.object2Json(map));
            JSONObject obj = JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 处理文本表情
    private static String dealTextContent(String content) {
        StringBuffer sb = new StringBuffer();
        if (content != null && content.indexOf("<img") != -1) {
            String[] s = content.split("<img");
            for (String str : s) {
                if (str.indexOf("mo-") != -1) {
                    str = str.substring(str.indexOf("mo-")).replace("mo-", "/").replace("\">", "");
                }
                sb.append(str);
            }
        } else {
            return content;
        }
        return sb.toString();
    }

}