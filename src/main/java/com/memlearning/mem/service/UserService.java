package com.memlearning.mem.service;

import com.hust.experiment.dao.LoginTicketDao;
import com.hust.experiment.dao.UserDao;
import com.hust.experiment.model.Exp;
import com.hust.experiment.model.LoginTicket;
import com.hust.experiment.model.User;
import com.hust.experiment.util.ExperimentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    LoginTicketDao loginTicketDao;

    public int addNewUser(User user){
        userDao.addUser(user);
        return user.getId();
    }

    public List<User> getAllUser(){
        List<User> list = userDao.getAllUser();
        return list;
    }

    public boolean hasUser(String account){
        return userDao.selectByAccount(account) != null;
    }

    //注册
    public Map<String,Object> register(String account,String password,String rePass){
        Map<String,Object> map = new HashMap<>();
        if (account.equals("")||account == null) {
            map.put("msgAccount", "学号/学工号不能为空");
            return map;
        }else if(userDao.selectByAccount(account) != null){
            map.put("msgAccount","此学号/学工号已被注册");
            return map;
        }else if(!account.matches("(U|M|T)\\d{9}")){
            map.put("msgAccount","学号/学工号格式不正确");
            return map;
        }

        if (password.equals("")||password == null) {
            map.put("msgPassword", "密码不能为空");
            return map;
        }

        if(!rePass.equals(password)){
            map.put("msgRepass","两次输入的密码不一致");
            return map;
        }

        User user = new User();
        user.setAccount(account);
        String position = "";
        char F = account.charAt(0);
        if(F == 'U'){
            position = "学生";
        }else if(F == 'T'){
            position = "教师";
        }else if(F == 'M'){
            position = "管理员";
        }
        user.setPosition(position);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setPassword(ExperimentUtil.MD5(password + user.getSalt()));
        user.setUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setName(account);
        userDao.addUser(user);
        return map;
    }

    //登录
    public Map<String,Object> login(String account,String password){
        Map<String,Object> map = new HashMap<>();

        if(account.equals("")||account == null){
            map.put("msgAccount","学号/学工号不能为空");
            return map;
        }
        User user = userDao.selectByAccount(account);
        if(user == null){
            map.put("msgAccount","该学号/学工尚未注册");
            return map;
        }
        if(password.equals("")||password == null){
            map.put("msgPwd","密码不能为空");
            return map;
        }
        if(!ExperimentUtil.MD5(password + user.getSalt()).equals(user.getPassword())){
            map.put("msgPed","密码不正确");
            return map;
        }
        String ticket = addLoginTicket(getUserbyAccount(account).getId());
        map.put("ticket",ticket);

        return map;
    }




}
