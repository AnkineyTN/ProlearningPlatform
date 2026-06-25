package com.cabybara.prolearningplatform.model.llm;

import com.cabybara.prolearningplatform.enums.LlmProvider;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * A user's Bring-Your-Own-Key (BYOK) LLM configuration. A user may store multiple configs but only
 * one may be active at a time (enforced by a partial unique index — see
 * {@code src/database/user_llm_config.sql}). The API key is stored AES-256-GCM encrypted; plaintext
 * is never persisted on this entity.
 */
@Entity
@Table(name = "user_llm_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLlmConfig extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_name", length = 64)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private LlmProvider provider;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    /** Base64( IV || AES-256-GCM ciphertext || tag ). */
    @Column(name = "api_key_encrypted", nullable = false, length = 1024)
    private String apiKeyEncrypted;

    /** Non-secret last 4 chars of the API key, used for masking on read. */
    @Column(name = "api_key_last4", length = 8)
    private String apiKeyLast4;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = false;
}
