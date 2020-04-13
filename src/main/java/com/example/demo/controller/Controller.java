package com.example.demo.controller;

import com.example.demo.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
public class Controller {
    @Autowired
    WebSocketService webSocketService;

    @RequestMapping("/push/{toUserId}")
    public ResponseEntity<String> pushToWeb(@PathVariable String toUserId) {
        for (int i = 0; i < 100; i++) {
            String msg = "ivy" + i;
            webSocketService.sendSelfInfo(msg,toUserId);
        }
        return ResponseEntity.ok("msg send success");
    }

    @RequestMapping("/page")
    @ResponseBody
    public ModelAndView page() {
        return new ModelAndView("pushMsg");
    }

}
