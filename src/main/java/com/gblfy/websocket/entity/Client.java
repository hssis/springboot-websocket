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
