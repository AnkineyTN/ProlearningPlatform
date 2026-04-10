package com.cabybara.prolearningplatform.model.note;

import com.cabybara.prolearningplatform.enums.NoteFileAttachmentKind;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note_file_region_comment")
public class NoteFileRegionComment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_note", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asset", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_kind", nullable = false, length = 16)
    private NoteFileAttachmentKind attachmentKind;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "rect_x", nullable = false)
    private double rectX;

    @Column(name = "rect_y", nullable = false)
    private double rectY;

    @Column(name = "rect_width", nullable = false)
    private double rectWidth;

    @Column(name = "rect_height", nullable = false)
    private double rectHeight;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "client_comment_id", length = 64)
    private String clientCommentId;

    /** Optional image (screenshot) uploaded via asset pipeline; not the same as {@link #asset} (PDF/page file). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_attachment_asset", referencedColumnName = "id")
    private Asset attachmentAsset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User author;
}
