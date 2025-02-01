package com.maillSaviour.demo.Constants;

import com.maillSaviour.demo.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

public class Constant {
    @Autowired
    private static MessageService messageService;

    public static final int LINK_NUMBER = 51;
    public static final String INTRO = "INTRO";
    public static final String UNAUTHORIZED_USER = "UNAUTHORIZED_USER";
    public static final String EXPIRED_USER = "EXPIRED_USER";
    public static final String DATA_LOST = messageService.getMessageContentByName(Constant.DATA_LOST);
    public static final String INVALID_SESSION = "INVALID_SESSION";
    public static final String EMAIL_EXCEEDED = "EMAIL_EXCEEDED";
    public static final String SENT_SUCCESSFULLY = "SENT_SUCCESSFULLY";
    public static final String APP_PASS_ID_FAILURE = "APP_PASS_ID_FAILURE";
    public static final String GREET = "GREET";
//    public static final String RECHARGE_REMINDER = "RECHARGE_REMINDER";
    public static final String PLAN_REMINDER = "PLAN_REMINDER";


    public static final String VALIDATION_FAILED_MESSAGE = "Fill the fields as directed in the warnings.";
    public static final String INTERRUPTION = "Email sending interrupted.";

    //Paths
    public static final String MAIL_PATH = "/sendGmail";
    public static final String GREET_PATH = "/messages/greet";
    public static final String LOGIN_PATH = "/loginForm";
    public static final String SESSION_ID = "sessionId";
    public static final String INTRO_PATH = "/messages/intro";
    public static final String IP_PATH = "/ipYhanHai";
    public static final String USER = "userName";
    public static final String PASSWORD = "password";
    public static final String STATUS = "status";
    public static final String REDIRECT_URL = "redirectUrl";
    public static final String MAIL_ENGINE_URL = "/sendGmail.html";
    public static final String MESSAGE = "message";
    public static final String LOGOUT_PATH = "/customLogout";
    public static final String LOGIN_URL = "/login.html";
    public static final String HOST_PATH = "/testHost";


    //HOST
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 587;
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String SMTP_AUTH_STATTLS_VAL = "true";

    //Retries and size
    public static final int BATCH_SIZE = 30;
    public static final int MAX_RETRIES = 3;
    public static final String TEMP_ERROR_STARTING = "4";
    public static final String ERROR_PATTERN = "\\b(4\\d{2}|5\\d{2})\\b";
    public static final long THREAD_SLEEP = 200L;
    public static final int MAX_TESTING_ID = 5;
    public static final int ALERT_BEFORE = 6;
    public static final String EXISTING_BODY = "This body is already associated with another user.";
    public static final String DEFAULT_SESSION_ID = "997e73fe-eb63-40c7-ba22-c7b2ffd03633";


}