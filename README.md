#背景：
根据公司需求在SpringBoot项目中集成站内信，于是，我做了一个SpringBoot2.x 整合websocket 消息推送，给指定用户发送信息和群发信息即点点对方式和广播方式2种模式。

# 一、地址部署总览

服务端：loclahost:8086/admin

客户端: loclahost:8086/index

# 二、实战需求案例
```aidl
服务端	实例1个
客户端A	实例1
客户端B	实例2
客户端C	实例3
```
# 三、实战准备
## 3.1. pom依赖
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.gblfy</groupId>
    <artifactId>springboot-websocket</artifactId>
    <version>v1.0.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
    </parent>

    <properties>
        <!--编码同意设置-->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!--JDK版本-->
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!--SpringMVC启动器-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--热部署插件-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
        </dependency>
        <!--websocket启动器-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <!--thymeleaf 模板引擎启动器-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <!--maven 打包编译插件-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```
## 3.2. application.yml
```xml
server:
  port: 80
spring:
    devtools:
      restart:
        exclude:  static/**,public/**
        enabled:  true
```
## 3.3. 配置类
```java
package com.gblfy.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启websocket的支持
 *
 * @Author gblfy
 * @Email gbly02@gmail.com
 * @Date 2019/11/20 PM 23:50
 */
@Configuration  
public class WebSocketConfig {  
    @Bean  
    public ServerEndpointExporter serverEndpointExporter(){  
        return new ServerEndpointExporter();  
    }  
}  
```
## 3.4. 实体类
```java
package com.gblfy.websocket.entity;

import javax.websocket.Session;
import java.io.Serializable;

/**
 * @Author gblfy
 * @Email gbly02@gmail.com
 * @Date 2019/11/20 PM 23:50
 */
public class Client implements Serializable {

    private static final long serialVersionUID = 8957107006902627635L;

    private String userName;

    private Session session;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Client(String userName, Session session) {
        this.userName = userName;
        this.session = session;
    }

    public Client() {
    }
}
```
## 3.5. websocket 服务端
```java
package com.gblfy.websocket.server;

import com.gblfy.websocket.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 *
 * @Author gblfy
 * @Email gbly02@gmail.com
 * @Date 2019/11/20 PM 23:50
 */
@ServerEndpoint(value = "/socketServer/{userName}")
@Component
public class SocketServer {

	private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	/**
	 *
	 * 用线程安全的CopyOnWriteArraySet来存放客户端连接的信息
	 */
	private static CopyOnWriteArraySet<Client> socketServers = new CopyOnWriteArraySet<>();

	/**
	 *
	 * websocket封装的session,信息推送，就是通过它来信息推送
	 */
	private Session session;

	/**
	 *
	 * 服务端的userName,因为用的是set，每个客户端的username必须不一样，否则会被覆盖。
	 * 要想完成ui界面聊天的功能，服务端也需要作为客户端来接收后台推送用户发送的信息
	 */
	private final static String SYS_USERNAME = "niezhiliang9595";


	/**
	 *
	 * 用户连接时触发，我们将其添加到
	 * 保存客户端连接信息的socketServers中
	 *
	 * @param session
	 * @param userName
	 */
	@OnOpen
	public void open(Session session,@PathParam(value="userName")String userName){

			this.session = session;
			socketServers.add(new Client(userName,session));

			logger.info("客户端:【{}】连接成功",userName);

	}

	/**
	 *
	 * 收到客户端发送信息时触发
	 * 我们将其推送给客户端(niezhiliang9595)
	 * 其实也就是服务端本身，为了达到前端聊天效果才这么做的
	 *
	 * @param message
	 */
	@OnMessage
	public void onMessage(String message){

		Client client = socketServers.stream().filter( cli -> cli.getSession() == session)
				.collect(Collectors.toList()).get(0);
		sendMessage(client.getUserName()+"<--"+message,SYS_USERNAME);

		logger.info("客户端:【{}】发送信息:{}",client.getUserName(),message);
	}

	/**
	 *
	 * 连接关闭触发，通过sessionId来移除
	 * socketServers中客户端连接信息
	 */
	@OnClose
	public void onClose(){
		socketServers.forEach(client ->{
			if (client.getSession().getId().equals(session.getId())) {

				logger.info("客户端:【{}】断开连接",client.getUserName());
				socketServers.remove(client);

			}
		});
	}

	/**
	 *
	 * 发生错误时触发
	 * @param error
	 */
    @OnError
    public void onError(Throwable error) {
		socketServers.forEach(client ->{
			if (client.getSession().getId().equals(session.getId())) {
				socketServers.remove(client);
				logger.error("客户端:【{}】发生异常",client.getUserName());
				error.printStackTrace();
			}
		});
    }

	/**
	 *
	 * 信息发送的方法，通过客户端的userName
	 * 拿到其对应的session，调用信息推送的方法
	 * @param message
	 * @param userName
	 */
	public synchronized static void sendMessage(String message,String userName) {

		socketServers.forEach(client ->{
			if (userName.equals(client.getUserName())) {
				try {
					client.getSession().getBasicRemote().sendText(message);

					logger.info("服务端推送给客户端 :【{}】",client.getUserName(),message);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 *
	 * 获取服务端当前客户端的连接数量，
	 * 因为服务端本身也作为客户端接受信息，
	 * 所以连接总数还要减去服务端
	 * 本身的一个连接数
	 *
	 * 这里运用三元运算符是因为客户端第一次在加载的时候
	 * 客户端本身也没有进行连接，-1 就会出现总数为-1的情况，
	 * 这里主要就是为了避免出现连接数为-1的情况
	 *
	 * @return
	 */
	public synchronized static int getOnlineNum(){
		return socketServers.stream().filter(client -> !client.getUserName().equals(SYS_USERNAME))
				.collect(Collectors.toList()).size();
	}

	/**
	 *
	 * 获取在线用户名，前端界面需要用到
	 * @return
	 */
	public synchronized static List<String> getOnlineUsers(){

		List<String> onlineUsers = socketServers.stream()
				.filter(client -> !client.getUserName().equals(SYS_USERNAME))
				.map(client -> client.getUserName())
				.collect(Collectors.toList());

	    return onlineUsers;
	}

	/**
	 *
	 * 信息群发，我们要排除服务端自己不接收到推送信息
	 * 所以我们在发送的时候将服务端排除掉
	 * @param message
	 */
	public synchronized static void sendAll(String message) {
		//群发，不能发送给服务端自己
		socketServers.stream().filter(cli -> cli.getUserName() != SYS_USERNAME)
				.forEach(client -> {
			try {
				client.getSession().getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		logger.info("服务端推送给所有客户端 :【{}】",message);
	}

	/**
	 *
	 * 多个人发送给指定的几个用户
	 * @param message
	 * @param persons
	 */
	public synchronized static void SendMany(String message,String [] persons) {
		for (String userName : persons) {
			sendMessage(message,userName);
		}
	}
}
```
## 3.6. 控制器
```java
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
        List<String> list = socketServer.getOnlineUsers();

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
```
## 3.7. SpringBoot入口类
```java
package com.gblfy.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot 启动器入口
 */
@SpringBootApplication
public class SpringBootWebSocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebSocketApplication.class,args);
    }
}
```
# 四、初始化页面总览
## 4.1. 服务端
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120232435654.png)
## 4.2. 客户端A
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233002542.png)
## 4.3. 客户端B
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233239145.png)
## 4.4. 客户端C
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233307284.png)
# 五、案例实战
## 5.1. 客户端A连接服务端
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233407591.png)
## 5.2. 客户端B连接服务端
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233453147.png)
## 5.3. 客户端C连接服务端
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233514842.png)
## 5.4. 服务端连接状态ABC
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233532223.png)
# 六、单独发送信息
## 6.1.  服务端给指定客户端A发送消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019112023363562.png)
## 6.2.  验证客户端A消息是否收到
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233652707.png)
# 七、群发信息
## 7.1. 给在线客户端群发消息
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233736437.png)
## 7.2.  客户端A 消息验证
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233802557.png)
## 7.2.  客户端B 消息验证
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233752629.png)
## 7.3.  客户端C 消息验证
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191120233816139.png)
从以上图中可以看出，测试符合预期。

博客地址：https://blog.csdn.net/weixin_40816738/article/details/103174517
想要学习更多微服务技术知识，请访问https://gblfy.com主页


-----
# 原理讲解

# 二、功能简介：
服务端当前在线数和在线人不是异步的，接受客户端的信息是异步的，所有在所有用户连接完成后刷新一次服务端就好

支持给一人推送信息，多人推送以及全部推送 个人推送和多人推送只要在多选框选择要推送的人，然后点击发送  全部推送 只需点击全部发送就好


### Websocket

Q: Websocket是什么？

A: 是一种在单个TCP连接上进行全双工通信的协议。

Q: 作用是什么？

A: 使得客户端和服务器之间的数据交换变得更加简单，允许服务端主动向客户端推送数据。在WebSocket API中，
浏览器和服务器只需要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。

Q: 优缺点是什么？

A：优点：客户端与服务端之间通讯取代了原先轮询的方式，能更好的节省服务器资源和带宽，并且能够更实时地进行通讯。
   缺点: 不支持低端的浏览器，这种情况就只能采用之前的轮询方式。
   
   
```js
//传统轮询询问方式
var ask = function() {
    
  $.ajax({
        url:'轮询地址',
        data:"user=client1",
        type:'post',
        success:function(res){
          if(res.data.status == true){
      
            console.log('服务端推送信息:%s',res.data.message)
            //TODO 对服务端推送的信息进行处理
            //清除定时器
            clearInterval(loopAsk);
          }
        }
      })
}

var loopAsk = setTimeout(ask,1000);

                                   
客户端:我是普通用户客户端1有我的信息嘛
                                          服务端:我查一下,没有                                          
客户端:我是普通用户客户端1有我的信息嘛
                                          服务端:我再查一下,还是没有
客户端:我是普通用户客户端1有我的信息嘛
                                          服务端:我再查一下,还是没有(已经有点不耐烦)                                         
客户端:我是普通用户客户端1有我的信息嘛
                                          服务端:我再查一下,谢天谢地,有了,终于可以摆脱这个穷逼啦.信息内容是:你妈叫你回家吃饭
最后客户端拿着信息回家吃饭啦
```   

```js
//websocket方式
var ws = null;

//和服务端进行连接的
if ('WebSocket' in window){
   ws = new WebSocket("ws://127.0.0.1:8086/socketServer/client2");
   
} else{
    console.log("该浏览器不支持websocket");    
}   

//接受来自服务端推送的信息
ws.onmessage = function(evt) {
    
    console.log('服务端推送信息:%s',evt.data)
    
};    

//断开连接触发	        
ws.onclose = function(evt) {
    
   console.log("client2成功断开服务端") 
    
};    

//和服务端连接成功触发	        
ws.onopen = function(evt) {
    
    console.log("client2连接服务端成功")
    
};  

客户端2: 那谁你过来一下,我要在你这办个会员,有我的信息记得发送给我。
                                                        服务端：好的,老板请把你的联系方式留下。

                      此时有一条某某发送给客户端2的信息,看到信息后服务端立马拿起啦手中的电话打给客户端2
                      
                                                        服务端：老板，这里有条您的信息，内容是：老公，你单手开法拉利的样子真帅。
                                                        
                      此时又有一条某某发送给客户端2的信息,看到信息后服务端立马拿起啦手中的电话打给客户端2
                      
                                                        服务端：老板，这里有条您的信息，内容是： xxxxx                      
              
```
