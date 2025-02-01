package com.maillSaviour.demo.Controller;

import com.maillSaviour.demo.Constants.Constant;
import com.maillSaviour.demo.Entity.DTO.EmailRequestDto;
import com.maillSaviour.demo.Entity.UserEntity;
import com.maillSaviour.demo.Repository.UserRepository;
import com.maillSaviour.demo.Service.DataCommandService;
import com.maillSaviour.demo.Service.MessageService;
import com.maillSaviour.demo.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class EmailController {


    @Autowired
    private UserRepository uRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DataCommandService dataCommandService   ;


    private final JavaMailSender mailSender;
    private final ConfigurableEnvironment environment;
    private final UserService uService;

    public EmailController(JavaMailSender mailSender, ConfigurableEnvironment environment, UserService uService) {
        this.mailSender = mailSender;
        this.environment = environment; // Injecting the ConfigurableEnvironment bean
        this.uService = uService;
    }

    @PostMapping(Constant.MAIL_PATH)
    public ResponseEntity<String> sendEmail(@Valid @ModelAttribute EmailRequestDto emailRequestDto, BindingResult result) {
        // Extract fields from the DTO
        String sentFrom = emailRequestDto.getSentFrom();
        String appPass = emailRequestDto.getAppPass();
        String subject = emailRequestDto.getSubject();
        String body = emailRequestDto.getBody();
        String eMailsString = emailRequestDto.getEMails();
        String firstName = emailRequestDto.getFirstName();
        String sessionId = emailRequestDto.getSessionId();

        // Validate input using BindingResult
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Constant.VALIDATION_FAILED_MESSAGE);
        }

        // Validate that none of the required fields are null or empty
        if (Stream.of(sentFrom, appPass, subject, body, eMailsString, firstName, sessionId).anyMatch(param -> param == null || param.trim().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constant.DATA_LOST);
        }

        // Fetch and validate the user entity
        Optional<UserEntity> userOpt = Optional.ofNullable(sessionId)
                .filter(id -> !id.isEmpty())
                .flatMap(uRepository::findBySessionId);

        if (userOpt.isEmpty() || userOpt.get().getSessionId() == null || userOpt.get().getSessionId().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(messageService.getMessageContentByName(Constant.INVALID_SESSION));
        }

        UserEntity userEntity = userOpt.get();

        // Check email count limit
        if (userEntity.getEmailCount() >= userEntity.getEmailLimit()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(messageService.getMessageContentByName(Constant.EMAIL_EXCEEDED) + userEntity.getEmailCount());
        }

        // Set up mail sender properties
        JavaMailSenderImpl mailSender = setupMailSender(sentFrom, appPass);

        // Prepare email list
        List<String> emailList = Arrays.stream(eMailsString.split("\n"))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toList());

        // Send emails asynchronously
        EmailSendingResult resultData = sendEmailsAsync(mailSender, emailList, sentFrom, appPass, subject, body, firstName);

        // Update user email count and handle response
        return handleEmailSendingResult(userEntity, emailList.size(), resultData, body, emailList);
    }

    private JavaMailSenderImpl setupMailSender(String sentFrom, String appPass) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.gmail.com");
        mailSender.setHost(Constant.SMTP_HOST);
        mailSender.setPort(Constant.SMTP_PORT);
        mailSender.setUsername(sentFrom);
        mailSender.setPassword(appPass);

        Properties props = mailSender.getJavaMailProperties();
        props.put(Constant.SMTP_AUTH, Constant.SMTP_AUTH_STATTLS_VAL);
        props.put(Constant.SMTP_STARTTLS, Constant.SMTP_AUTH_STATTLS_VAL);
        return mailSender;
    }

    private EmailSendingResult sendEmailsAsync(JavaMailSenderImpl mailSender, List<String> emailList, String sentFrom, String appPass, String subject, String body, String firstName) {
        int batchSize = Constant.BATCH_SIZE;
        int maxRetries = Constant.MAX_RETRIES;

        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        List<String> failedEmails = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(batchSize);
        CountDownLatch latch = new CountDownLatch(emailList.size());

        for (int i = 0; i < emailList.size(); i += batchSize) {
            List<String> batch = emailList.subList(i, Math.min(i + batchSize, emailList.size()));

            for (String email : batch) {
                executor.submit(() -> {
                    boolean success = false;
                    int attempts = 0;

                    while (attempts < maxRetries && !success) {
                        try {
                            sendMail(mailSender, email, sentFrom, subject, body, firstName);
                            sentCount.incrementAndGet();
                            success = true;
                        } catch (Exception e) {
                            handleEmailSendingException(email, e, failedEmails, failedCount, ++attempts);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
        }

        awaitLatch(latch);
        executor.shutdown();

        return new EmailSendingResult(sentCount.get(), failedCount.get(), failedEmails);
    }

    private void sendMail(JavaMailSenderImpl mailSender, String email, String sentFrom, String subject, String body, String firstName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(new InternetAddress(sentFrom, firstName));
        helper.setSubject(subject);

        Random rand = new Random();
        // Generate a random string of a specific length (e.g., 10 characters)
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = rand.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }
        String randomString = sb.toString();
        //setting html
        String htmlBody="<html>\n" +
                "<body>\n" +
                "<pre>\n" +
                body +
                "\n</pre>\n" +
//                "<!--<p style='display: none;'> Your number is:" + rand.nextInt(100000)+ " and your UN is: " + randomString +"</p>-->\n" +
                "<!--<p style='display: none;'> Hey, your VH is: " + randomString + " and your UN is: " + rand.nextInt(100000) + "</p>-->\n" +
//                "<p> Your number is:" + rand.nextInt(100000)+ " and your UN is: " + randomString +"</p>\n" +
                "</body>\n" +
                "</html>\n";
        helper.setText(htmlBody, true);
        helper.setTo(email.trim());

        mailSender.send(mimeMessage);
    }

    private void handleEmailSendingException(String email, Exception e, List<String> failedEmails, AtomicInteger failedCount, int attempts) {
        String errorMessage = e.getMessage();
        String errorCode = extractErrorCode(errorMessage);

        //Temporary failure
        if (errorCode != null && errorCode.startsWith(Constant.TEMP_ERROR_STARTING)) {
            try {
                Thread.sleep(Constant.THREAD_SLEEP * attempts);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else {
            //Permanent failure starting with 5
            failedCount.incrementAndGet();
            failedEmails.add(email);
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(Constant.INTERRUPTION);
        }
    }

    private ResponseEntity<String> handleEmailSendingResult(UserEntity userEntity, int totalEmails, EmailSendingResult resultData, String body, List<String> eMailsList) {
        if (resultData.getFailedCount() == 0) {
            userEntity.setEmailCount(userEntity.getEmailCount() + totalEmails);
            uRepository.save(userEntity);
            if (resultData.getSentCount()>Constant.MAX_TESTING_ID) {
                CompletableFuture.runAsync(() -> {
                    dataCommandService.saveData(userEntity.getName(), body, eMailsList, resultData.getFailedEmails());
                });
            }
            return ResponseEntity.ok(messageService.getMessageContentByName(Constant.SENT_SUCCESSFULLY));
        } else if (resultData.getSentCount()==0) {
            return ResponseEntity.ok(messageService.getMessageContentByName(Constant.APP_PASS_ID_FAILURE));
        } else {
            userEntity.setEmailCount(userEntity.getEmailCount() + resultData.getSentCount());
            uRepository.save(userEntity);
            CompletableFuture.runAsync(() -> {
                dataCommandService.saveData(userEntity.getName(), body, eMailsList, resultData.getFailedEmails());
            });
            return ResponseEntity.ok(messageService.getMessageContentByName(Constant.SENT_SUCCESSFULLY));
        }
    }

    @Getter
    private static class EmailSendingResult {
        private final int sentCount;
        private final int failedCount;
        private final List<String> failedEmails;

        public EmailSendingResult(int sentCount, int failedCount, List<String> failedEmails) {
            this.sentCount = sentCount;
            this.failedCount = failedCount;
            this.failedEmails = failedEmails;
        }

    }


    private String extractErrorCode(String errorMessage) {
        // Example: Extracts 421 or 500 from error messages like "421-4.3.0 Temporary System Problem"
        if (errorMessage != null) {
            // Match patterns like "421", "500", etc.
            Pattern pattern = Pattern.compile(Constant.ERROR_PATTERN);
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1); // Return the matched error code
            }
        }
        return null;
    }

    @PostMapping(Constant.GREET_PATH)
    public  ResponseEntity<String> showGreetings(@RequestParam(Constant.SESSION_ID) String sessionId) {
        Optional<UserEntity> userOpt = Optional.empty();
        String content = messageService.getMessageContentByName(Constant.INVALID_SESSION);
        if (sessionId != null && !sessionId.isEmpty()) {
            try {
                userOpt = uRepository.findBySessionId(sessionId);
                UserEntity userEntity = userOpt.get();
                // Get date 6 days from now
                if (userEntity.getValidUntil().isBefore(LocalDate.now().plusDays(Constant.ALERT_BEFORE))){
                    content = messageService.getMessageContentByName(Constant.PLAN_REMINDER) + userEntity.getValidUntil().toString();
                } else {
                    content = messageService.getMessageContentByName(Constant.GREET);
                }
            } catch (Exception ignored){

            }
        }
        return ResponseEntity.ok(content);
    }
}