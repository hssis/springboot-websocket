package com.gblfy.websocket.controller;

import com.gblfy.websocket.server.SocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * websocket
 * 消息推送(个人和广播)
 *
 * @Author gblfy
 * @Email gbly02@gmail.com
 * @Date 2019/11/20 PM 23:50
 */
@Controller
public class WebSocketController {

    @Autowired
    private SocketServer socketServer;

    /**
     *
     * 客户端页面
     * @return
     */
    @RequestMapping(value = "/index")
    public String idnex() {

        return "index";
    }

    /**
     *
     * 服务端页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/admin")
    public String admin(Model model) {
        int num = socketServer.getOnlineNum();
        System.out.println(num);
        List<String> list = socketServer.getOnlineUsers();
        System.out.println(list);

        model.addAttribute("num",num);
        model.addAttribute("users",list);
        return "admin";
    }

    /**
     * 个人信息推送
     * @return
     */
    @RequestMapping("sendmsg")
    @ResponseBody
    public String sendmsg(String msg, String username){
        //第一个参数 :msg 发送的信息内容
        //第二个参数为用户长连接传的用户人数
        String [] persons = username.split(",");
        SocketServer.SendMany(msg,persons);
        return "success";
    }

    /**
     * 推送给所有在线用户
     * @return
     */
    @RequestMapping("sendAll")
    @ResponseBody
    public String sendAll(String msg){
        SocketServer.sendAll(msg);
        return "success";
    }
}
