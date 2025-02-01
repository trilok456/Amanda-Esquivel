package com.maillSaviour.demo.Service;

import com.maillSaviour.demo.Constants.Constant;
import com.maillSaviour.demo.Entity.UserEntity;
import com.maillSaviour.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository uRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = uRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                new ArrayList<>());
    }

    public UserEntity getUserEntityByUsername(String username) {
        return uRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

     public int loginUser(String username, String password) {
         try {
             UserEntity user = getUserEntityByUsername(username);
             boolean isExpired = user.getValidUntil().isBefore(LocalDate.now());
             if (Objects.equals(password, user.getPassword())
//                     && Objects.equals(Constant.LINK_NUMBER, user.getLinkNumber())
                     && Objects.equals(getServiceHost(), user.getHost())
                     //request context holder (host details) and then change the DB to only accept the host details
                     && !isExpired
                     && user.getIsAssigned()) {
                     String sessionId = UUID.randomUUID().toString();
                     user.setSessionId(sessionId);
                     uRepository.save(user);
                     return HttpStatus.OK.value();
             } else if (isExpired){
                 return HttpStatus.PAYMENT_REQUIRED.value();
             } else {
                 return HttpStatus.UNAUTHORIZED.value();
             }
         } catch (Exception ignored) {
         }
         return HttpStatus.UNAUTHORIZED.value();
     }


    public void logoutUser(String sessionId) {
        UserEntity user = uRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        user.setSessionId(Constant.DEFAULT_SESSION_ID);
        uRepository.save(user);
    }

    public String getServiceHost(){
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            // Get the hostname
            return request.getServerName();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
//            return ResponseEntity.ok(e.getMessage());
        }
    }
}