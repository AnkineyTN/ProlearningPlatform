package com.cabybara.prolearningplatform.model.pomodoro;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "pomodoro_user_favorite_sound",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_sound"}))
public class PomodoroUserFavoriteSound extends AbstractEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sound", referencedColumnName = "id")
    private PomodoroSound sound;
}
