package com.cabybara.prolearningplatform.service.payment;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.payment.impl.SubscriptionServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private EmailService emailService;

    @Test
    void activateProUpdatesUserAccountType() {
        SubscriptionServiceImpl subscriptionService = new SubscriptionServiceImpl(
                userRepository, notificationDispatcher, emailService);

        User user = TestFixtures.user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        subscriptionService.activatePro(1L);

        assertEquals(AccountType.PRO, user.getAccountType());
        verify(userRepository).save(user);
    }

    @Test
    void activateProSendsNotificationAndEmail() {
        SubscriptionServiceImpl subscriptionService = new SubscriptionServiceImpl(
                userRepository, notificationDispatcher, emailService);

        User user = TestFixtures.user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        subscriptionService.activatePro(1L);

        verify(notificationDispatcher).dispatchToUser(anyLong(), anyString(), anyString(), any());
        verify(emailService).sendProUpgradeNotification(anyString(), anyString());
    }
}
