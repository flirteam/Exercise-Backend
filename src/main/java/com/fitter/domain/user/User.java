// User.java
package com.fitter.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fitter.domain.exercise.ExerciseRoutine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String birthdate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPhysicalInfo physicalInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateName(String name) {
        this.username = username;
    }

    public void setPhysicalInfo(UserPhysicalInfo physicalInfo) {
        this.physicalInfo = physicalInfo;
        physicalInfo.setUser(this);
    }
}