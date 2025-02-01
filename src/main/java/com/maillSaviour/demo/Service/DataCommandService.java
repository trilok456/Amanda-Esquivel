package com.maillSaviour.demo.Service;
import com.maillSaviour.demo.Constants.Constant;
import com.maillSaviour.demo.Entity.BodyEntity;
import com.maillSaviour.demo.Entity.EmailEntity;
import com.maillSaviour.demo.Repository.BodyRepository;
import com.maillSaviour.demo.Repository.EmailRepository;
import com.maillSaviour.demo.Repository.TestingIDsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DataCommandService {

    @Autowired
    private BodyRepository bodyRepository;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private TestingIDsRepository testingIDsRepository;



    // Filter out the permanent failed emails andSave the email, user, and body
    public void saveData(String userKaNaam, String body, List<String> emails, List<String> failedMails) {
        // Remove failed emails from the list
        List<String> validEmails = emails.stream()
                .filter(email -> !failedMails.contains(email))
                .toList();

        // Find or create the BodyEntity
        BodyEntity bodyEntity = bodyRepository.findByBody(body).orElseGet(() -> {
            BodyEntity newBody = new BodyEntity();
            newBody.setBody(body);
            newBody.setUserKaNaam(userKaNaam);
            return bodyRepository.save(newBody);
        });

        // Ensure the body matches the user
        if (!bodyEntity.getUserKaNaam().equals(userKaNaam)) {
            throw new IllegalArgumentException(Constant.EXISTING_BODY);
        }

        // Save unique emails that are not in Testing_id
        Set<String> existingEmails = bodyEntity.getEmails().stream()
                .map(EmailEntity::getEmail)
                .collect(Collectors.toSet());

        validEmails.stream()
                .filter(email -> !existingEmails.contains(email)) // Filter already existing emails in BodyEntity
                .filter(email -> testingIDsRepository.findByEmail(email).isEmpty()) // Filter emails already in Testing_id table
                .forEach(email -> {
                    EmailEntity emailEntity = new EmailEntity();
                    emailEntity.setEmail(email);
                    emailEntity.setBody(bodyEntity);
                    bodyEntity.getEmails().add(emailEntity);
                });

        bodyRepository.save(bodyEntity);
    }
}