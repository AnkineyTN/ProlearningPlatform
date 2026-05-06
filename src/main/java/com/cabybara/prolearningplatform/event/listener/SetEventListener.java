package com.cabybara.prolearningplatform.event.listener;

import com.cabybara.prolearningplatform.event.model.ChildEntityUpdatedEvent;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.utils.SetChild;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetEventListener {
    private final SetRepository setRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleChildEntityUpdate(ChildEntityUpdatedEvent event) {

        SetChild child = event.getChildEntity();
        if (child == null || child.getSet() == null) {
            log.warn("Event update not have parent set");
            return;
        }

        setRepository.updateLastModifiedDate(child.getSet().getId(), OffsetDateTime.now());
    }
}
