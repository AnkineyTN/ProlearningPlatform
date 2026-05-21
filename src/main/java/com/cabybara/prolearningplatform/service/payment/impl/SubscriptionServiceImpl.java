package com.cabybara.prolearningplatform.service.payment.impl;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.payment.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailService emailService;

    @Override
    public void activatePro(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setAccountType(AccountType.PRO);
        userRepository.save(user);

        notificationDispatcher.dispatchToUser(
                userId,
                NotificationType.ACCOUNT_UPGRADED.getDefaultTitle(),
                "Welcome to PRO! Enjoy unlimited access to all premium features.",
                NotificationType.ACCOUNT_UPGRADED
        );
        emailService.sendProUpgradeNotification(user.getEmail(), user.getUsername());
    }
}
