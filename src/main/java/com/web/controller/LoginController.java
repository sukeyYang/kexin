package com.web.controller;

import java.util.Date;

import com.utils.MD5Util;
import com.utils.StringUtil;
import com.web.Exception.UserOutNotFindException;
import com.web.entity.USER_OUT;
import com.web.entity.UserOutToken;
import com.web.model.ErrorModel;
import com.web.model.LoginModel;
import com.web.service.UserOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.web.bind.annotation.*;

/**
 * Created by sukey on 2016/6/25.
 */
@RestController
@RequestMapping("/userout")
public class LoginController {
    @Autowired
    UserOutService userOutService;

    @RequestMapping(value="/login",method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public LoginModel login (String username, String password){
        LoginModel model=new LoginModel();
        USER_OUT user =userOutService.findUserOutByUserName(username);
        if(user==null) {//用户不存在
            throw new UserOutNotFindException();
        }

        if(!user.getPassword().equals(password)) {//密码错误

        }
        model.setUserid(user.getUsername());
        String onceStr= StringUtil.getRandomString(8);
        String token= MD5Util.mmd5(user.getUsername()+onceStr+password);
        model.setToken(token);

        UserOutToken userOutToken=new UserOutToken();
        userOutToken.setToken(token);
        userOutToken.setCreate_time(new Date());
        userOutToken.setUsername(user.getUsername());
        userOutToken.setOncestr(onceStr);
        //保存token对象

        return model;

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorModel handleUserNotFindException(UserOutNotFindException e){
        return new ErrorModel("404","用户不存在");
    }


}