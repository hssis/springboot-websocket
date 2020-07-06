package com.gblfy.websocket.wxnotice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

/**
 * Created by qcl on 2019-03-28
 * 微信：2501902696
 * desc: 模版消息推送模拟
 */
@RestController
public class PushController {


    /*
     * 微信测试账号推送
     * */
    @GetMapping("/push")
    public static void push() {
        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId("wx6f20725cb674fa5d");
        wxStorage.setSecret("007bdc56507e285637d0312e808edd03");
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);

        //2,推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser("oAiR7t1m2a6EPtoANzacLSBuWWEU")//要推送的用户openid
                .templateId("raDy7KnpX9Y5umoBHzoNzm0N3rKaC76N1GPD7XruC6g")//模版id
                .url("https://30paotui.com/")//点击模版消息要访问的网址
                .build();
        //3,如果是正式版发送模版消息，这里需要配置你的信息
        //        templateMessage.addData(new WxMpTemplateData("name", "value", "#FF00FF"));
        //                templateMessage.addData(new WxMpTemplateData(name2, value2, color2));
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        push();
    }


}
