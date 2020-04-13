package com.example.demo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Component
@ServerEndpoint("/server/{userId}")
public class WebSocketService {
    AtomicInteger onlineCount = new AtomicInteger();
    private static ConcurrentHashMap<String, WebSocketService> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String userId = "";

    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        } else {
            webSocketMap.put(userId, this);
            onlineCount.incrementAndGet();
        }
        try {
            sendMessage("connnect success");
        } catch (IOException e) {
            System.out.println("connect fail");
        }
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            onlineCount.decrementAndGet();
        }
    }

    /**
     * 收到客户端消息后调用方法
     */
    @OnMessage
    public void onMessage(Session session,String msg) {
        if (StringUtils.isNotBlank(msg)) {
            // 解析消息
            JSONObject jsonObject = JSON.parseObject(msg);
            // 追加发送人（防止串改）
            jsonObject.put("fromUserId", this.userId);
            String toUserId = jsonObject.getString("toUserId");
            if (StringUtils.isNotBlank(toUserId) && webSocketMap.containsKey(toUserId)) {
                try {
                    webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //todo 发送到redis
            }
        }
    }

    /**
     * 建立连接错误
     */
    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    /**
     * 服务器推送消息
     * @param msg
     */
    private void sendMessage(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }

    /**
     * 发送自定义消息
     */
    public void sendSelfInfo(String msg,@PathParam("userId") String userId) {
        if (StringUtils.isNotBlank(userId) && webSocketMap.containsKey(userId)) {
            try {
                webSocketMap.get(userId).sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("connect fail");
        }
    }

}
