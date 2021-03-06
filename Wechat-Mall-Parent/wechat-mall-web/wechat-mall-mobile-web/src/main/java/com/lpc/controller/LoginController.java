package com.lpc.controller;

import com.lpc.constants.BaseResponseConstants;
import com.lpc.constants.Constants;
import com.lpc.feign.UserFeign;
import com.lpc.utils.ControllerUtils;
import com.lpc.utils.CookieUtil;
import com.lpc.utils.ResultUtils;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.oauth.Oauth;
import lombok.extern.slf4j.Slf4j;
import member.entity.mb_user;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Lin
 * @Date 2019/12/16
 */
@Controller
@Slf4j
public class LoginController {
    private static final String LOGIN = "login";
    private static final String ERROR = "error";
    private static final String ASSOCIATEDACCOUNT = "associatedAccount";
    private static final String FORGOTPWD = "forgotPwd";
    private static final String UPDATEPWD = "updatePwd";
    @Autowired
    private UserFeign userFeign;

    @RequestMapping("/toLogin")
    public String toLogin(HttpServletRequest request,
                          @RequestParam(required = false,name = "source") String source){
        if (!StringUtils.isEmpty(source)){
            request.getSession().setAttribute("source",source);
        }
        return LOGIN;
    }

    @RequestMapping("/login")
    public String login(mb_user mb_user, HttpServletRequest request,
                        HttpServletResponse response){
        //关联已有账户 需要封装openID
        String source = (String) request.getSession().getAttribute("source");
        if (!StringUtils.isEmpty(source) && source.equals("qq")){
            String openID = (String) request.getSession().getAttribute(Constants.USER_OPENID);
            mb_user.setOpenID(openID);
        }
        //调用feign客户端实现登录功能   返回json信息(code、msg)
        Map<String, Object> map = userFeign.login(mb_user);
        //判断是否登录成功
        String token = (String)ResultUtils.getResultMap(map);
        if (StringUtils.isEmpty(token)){
            return ControllerUtils.setError(request,(String) map.get(BaseResponseConstants.HTTP_RESP_CODE_MSG),LOGIN);
        }
        //登录成功后，将token存入cookie中  如果是qq授权登录则需要清除session中的openID
        CookieUtil.addCookie(response, Constants.USER_TOKEN,token, Constants.WEB_USER_COOKIE_MAX_AGE);
        //清除session中的openID,防止重复使用
        if (request.getSession().getAttribute(Constants.USER_OPENID) != null){
            request.getSession().removeAttribute(Constants.USER_OPENID);
        }
        return "redirect:http://28ii870524.zicp.vip/index-web/index";
    }

    /**
     * 生成qq授权地址并进行跳转到qq授权页面
     */
    @RequestMapping("/authorizeURL")
    public String authorizeURL(HttpServletRequest request){
        String authorizeURL = null;
        try {
            //直接调用api生成一个授权的url
            authorizeURL = new Oauth().getAuthorizeURL(request);
        } catch (QQConnectException e) {
            e.printStackTrace();
        }
        //重定向到当前页面下的url资源
        return "redirect:" + authorizeURL;
    }

    /**
     * 点击qq用户头像 即开始授权 并检查是否与数据库用户信息绑定 分四步
     * 1.获取授权码
     * 2.获取accessToken
     * 3.获取openID
     * 4.根据userOpenID进入数据库看是否能查询到用户信息，即是否与数据库中的openID关联
     */
    @RequestMapping("/qqLoginBack")
    public String qqLoginBack(HttpServletRequest request, HttpServletResponse response) throws QQConnectException {
        AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);
        String accessToken = accessTokenObj.getAccessToken();
        if (StringUtils.isEmpty(accessToken)){
            return ControllerUtils.setError(request,"获取accessToken为空，QQ授权失败!",ERROR);
        }
        OpenID openIDObj = new OpenID(accessToken);
        String openID = openIDObj.getUserOpenID();
        //把openID保存到session,便于qq登录关联用户信息
        request.getSession().setAttribute(Constants.USER_OPENID,openID);
        Map<String, Object> userByLoginOpenIDMap = userFeign.getUserByLoginOpenID(openID);
        Integer code = (Integer) userByLoginOpenIDMap.get(BaseResponseConstants.HTTP_RESP_CODE_NAME);
        //有关联则直接登录 存cookie
        if (code.equals(BaseResponseConstants.HTTP_RESP_CODE_200)){
            //获取token
            String token = (String) userByLoginOpenIDMap.get(BaseResponseConstants.HTTP_RESP_CODE_DATA);
            //存cookie
            CookieUtil.addCookie(response, Constants.USER_TOKEN,token, Constants.WEB_USER_COOKIE_MAX_AGE);
            return "redirect:http://28ii870524.zicp.vip/index-web/index";
        }
        //无关联则跳转到绑定页面
        return ASSOCIATEDACCOUNT;
    }

    /**
     * 忘记密码
     */
    @RequestMapping("/forgotPwd")
    public String forgotPwd(){
        return FORGOTPWD;
    }

    /**
     * 找回密码
     */
    @RequestMapping("/findPassword")
    public String findPassword(HttpServletRequest request,@RequestParam("email")String email){
        Map<String, Object> map = userFeign.findPassword(email);
        if (map.get(BaseResponseConstants.HTTP_RESP_CODE_NAME).equals(BaseResponseConstants.HTTP_RESP_CODE_500)){
            return ControllerUtils.setError(request,(String) map.get(BaseResponseConstants.HTTP_RESP_CODE_MSG),ERROR);
        }
        request.setAttribute("error","稍后几秒钟，链接将发往您的邮箱");
        return LOGIN;
    }

    /**
     * 修改密码的链接
     */
    @RequestMapping("/toUpdatePwd")
    public String toUpdatePwd(){
        return UPDATEPWD;
    }

    /**
     * 修改密码
     */
    @RequestMapping("/updatePwd")
    public String updatePwd(HttpServletRequest request,@RequestParam("phone")String phone,@RequestParam("password")String password){
        Map<String, Object> map = userFeign.updatePwd(phone, password);
        if (!map.get(BaseResponseConstants.HTTP_RESP_CODE_NAME).equals(BaseResponseConstants.HTTP_RESP_CODE_200)){
            return ControllerUtils.setError(request,"手机号不存在",UPDATEPWD);
        }
        request.setAttribute("error","修改成功，请重新登录");
        return LOGIN;
    }

}
