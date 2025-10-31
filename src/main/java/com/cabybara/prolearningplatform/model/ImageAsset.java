package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.ImageStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image_asset")
public class ImageAsset extends AbstractEntity {
    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ImageStatus status = ImageStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    @JsonIgnore
    private User user;
}
