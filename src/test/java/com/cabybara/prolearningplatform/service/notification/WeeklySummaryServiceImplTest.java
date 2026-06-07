package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;
import com.cabybara.prolearningplatform.repository.SetNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.impl.WeeklySummaryProcessor;
import com.cabybara.prolearningplatform.service.notification.impl.WeeklySummaryServiceImpl;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class WeeklySummaryServiceImplTest {

    @Mock
    private SetNotificationPreferenceRepository setNotificationPreferenceRepository;

    @Mock
    private WeeklySummaryProcessor processor;

    @Test
    @DisplayName("processWeeklySummaries should skip processor when no preferences match today")
    void processWeeklySummariesShouldSkipWhenNoPreferences() {
        WeeklySummaryServiceImpl service = new WeeklySummaryServiceImpl(setNotificationPreferenceRepository, processor);
        when(setNotificationPreferenceRepository.findAllByWeeklySummaryDay(anyInt())).thenReturn(List.of());

        service.processWeeklySummaries();

        verify(processor, never()).processSingleSet(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("processWeeklySummaries should continue after processor failure for lazy set proxy")
    void processWeeklySummariesShouldContinueAfterProcessorFailure() {
        WeeklySummaryServiceImpl service = new WeeklySummaryServiceImpl(setNotificationPreferenceRepository, processor);
        SetNotificationPreference failingPreference = mock(SetNotificationPreference.class);
        SetNotificationPreference succeedingPreference = preference(22L, 8L);
        Set failingSetProxy = lazySetProxy(117L);

        when(failingPreference.getSet()).thenReturn(failingSetProxy);
        when(setNotificationPreferenceRepository.findAllByWeeklySummaryDay(anyInt()))
                .thenReturn(List.of(failingPreference, succeedingPreference));
        when(processor.processSingleSet(failingPreference)).thenThrow(new RuntimeException("boom"));
        when(processor.processSingleSet(succeedingPreference)).thenReturn(true);

        assertThatNoException().isThrownBy(service::processWeeklySummaries);

        verify(processor).processSingleSet(failingPreference);
        verify(processor).processSingleSet(succeedingPreference);
    }

    @Test
    @DisplayName("sendWeeklySummaryToUser should continue after processor failure for lazy set proxy")
    void sendWeeklySummaryToUserShouldContinueAfterProcessorFailure() {
        WeeklySummaryServiceImpl service = new WeeklySummaryServiceImpl(setNotificationPreferenceRepository, processor);
        SetNotificationPreference failingPreference = mock(SetNotificationPreference.class);
        SetNotificationPreference succeedingPreference = preference(33L, 5L);
        Set failingSetProxy = lazySetProxy(117L);

        when(failingPreference.getSet()).thenReturn(failingSetProxy);
        when(setNotificationPreferenceRepository.findEnabledByUserId(5L))
                .thenReturn(List.of(failingPreference, succeedingPreference));
        when(processor.processSingleSet(failingPreference)).thenThrow(new RuntimeException("boom"));
        when(processor.processSingleSet(succeedingPreference)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> service.sendWeeklySummaryToUser(5L));

        verify(processor).processSingleSet(failingPreference);
        verify(processor).processSingleSet(succeedingPreference);
    }

    private SetNotificationPreference preference(Long setId, Long userId) {
        User user = new User();
        user.setId(userId);

        Set set = new Set();
        set.setId(setId);
        set.setUser(user);

        SetNotificationPreference preference = new SetNotificationPreference();
        preference.setSet(set);
        return preference;
    }

    private Set lazySetProxy(Long setId) {
        Set setProxy = mock(Set.class, withSettings().extraInterfaces(HibernateProxy.class));
        LazyInitializer lazyInitializer = mock(LazyInitializer.class);

        doReturn(lazyInitializer).when((HibernateProxy) setProxy).getHibernateLazyInitializer();
        when(lazyInitializer.getIdentifier()).thenReturn(setId);

        return setProxy;
    }
}
