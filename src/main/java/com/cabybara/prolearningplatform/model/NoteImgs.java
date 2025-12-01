package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.model.composite_key.NoteDocsId;
import com.cabybara.prolearningplatform.model.composite_key.NoteImgsId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note_imgs")
public class NoteImgs {
    @EmbeddedId
    private NoteImgsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("noteId")
    @JoinColumn(name = "id_note", nullable = false)
    private Note note;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("assetId")
    @JoinColumn(name = "id_asset", nullable = false)
    private Asset asset;

    public NoteImgs(Note note, Asset asset) {
        this.note = note;
        this.asset = asset;
        if (note != null && asset != null) {
            this.id = new NoteImgsId(note.getId(), asset.getId());
        }
    }
}
