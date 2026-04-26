package com.cabybara.prolearningplatform.model.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
@Entity
@Table(name = "pomodoro_space")
public class PomodoroSpace extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "source", nullable = false)
    @Builder.Default
    private String source = "SYSTEM";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asset", referencedColumnName = "id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}