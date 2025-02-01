package com.maillSaviour.demo.Controller;


import com.maillSaviour.demo.Constants.Constant;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Controller
public class IpController {
    @GetMapping(Constant.IP_PATH)
    public ResponseEntity<String> getIP() throws UnknownHostException {
        return ResponseEntity.ok(InetAddress.getLocalHost().getHostAddress());
    }
}