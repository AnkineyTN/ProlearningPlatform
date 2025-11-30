package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.AssetStatus;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asset")
public class Asset extends AbstractEntity {
    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AssetStatus status = AssetStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @Builder.Default
    private AssetType type = AssetType.IMAGE;

    @Column(name = "file_name", nullable = false, length = 1024)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    @JsonIgnore
    private User user;
}
