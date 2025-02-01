package com.maillSaviour.demo.Controller;

import com.maillSaviour.demo.Constants.Constant;
import com.maillSaviour.demo.Entity.UserEntity;
import com.maillSaviour.demo.Service.MessageService;
import com.maillSaviour.demo.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService uService;

    @Autowired
    private MessageService messageService;

    @GetMapping(Constant.INTRO_PATH)
    public  ResponseEntity<String> showIntro() {
        String content = messageService.getMessageContentByName(Constant.INTRO);
        return ResponseEntity.ok(content);
    }

    @PostMapping(Constant.LOGIN_PATH)
    public ResponseEntity<Map<String, Object>> customLogin(@RequestParam(Constant.USER) String userName,
                                                     @RequestParam(Constant.PASSWORD) String password) {
        int sResponse = uService.loginUser(userName, password);
        Map<String, Object> response = new HashMap<>();

        if (sResponse == HttpStatus.OK.value()) {
            // Fetch the session ID from the user service.
            UserEntity user = uService.getUserEntityByUsername(userName); // Assuming you have this method available
            response.put(Constant.STATUS, HttpStatus.OK.value());
            response.put(Constant.SESSION_ID, user.getSessionId());
            response.put(Constant.REDIRECT_URL, Constant.MAIL_ENGINE_URL);
        }  else if (sResponse==HttpStatus.PAYMENT_REQUIRED.value()) {
                response.put(Constant.MESSAGE, messageService.getMessageContentByName(Constant.EXPIRED_USER));
                response.put(Constant.STATUS, HttpStatus.PAYMENT_REQUIRED.value());
                response.put(Constant.REDIRECT_URL, null);
        } else {
              response.put(Constant.STATUS, HttpStatus.UNAUTHORIZED.value());
              response.put(Constant.REDIRECT_URL, null);
              response.put(Constant.MESSAGE, messageService.getMessageContentByName(Constant.UNAUTHORIZED_USER));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constant.LOGOUT_PATH)
    public RedirectView customlogout(@RequestParam(Constant.SESSION_ID) String sessionId) {
        try {
            uService.logoutUser(sessionId);
            return new RedirectView(Constant.LOGIN_URL);
        } catch (Exception e) {
            return new RedirectView(Constant.LOGIN_URL);
        }
    }

    @GetMapping(Constant.HOST_PATH)
    public ResponseEntity<String> customHostGiver() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String serverName = request.getServerName(); // Get the hostname
            return ResponseEntity.ok(serverName);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.ok(e.getMessage());
        }
    }
}