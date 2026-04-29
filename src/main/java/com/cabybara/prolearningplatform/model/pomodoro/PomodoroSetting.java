package com.cabybara.prolearningplatform.model.pomodoro;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "pomodoro_setting")
public class PomodoroSetting extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", unique = true)
    private User user;

    @Column(name = "pomodoro_duration", nullable = false)
    @Builder.Default
    private Integer pomodoroDuration = 1500;

    @Column(name = "short_break", nullable = false)
    @Builder.Default
    private Integer shortBreak = 300;

    @Column(name = "long_break", nullable = false)
    @Builder.Default
    private Integer longBreak = 900;

    @Column(name = "long_break_interval", nullable = false)
    @Builder.Default
    private Integer longBreakInterval = 4;

    @Column(name = "auto_start_break", nullable = false)
    @Builder.Default
    private Boolean autoStartBreak = false;

    @Column(name = "auto_start_pomodoro", nullable = false)
    @Builder.Default
    private Boolean autoStartPomodoro = false;
}