package com.cabybara.prolearningplatform.model.note;

import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.composite_key.NoteDocsId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note_docs")
public class NoteDocs {
    @EmbeddedId
    private NoteDocsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("noteId")
    @JoinColumn(name = "id_note", nullable = false)
    private Note note;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("assetId")
    @JoinColumn(name = "id_asset", nullable = false)
    private Asset asset;

    public NoteDocs(Note note, Asset asset) {
        this.note = note;
        this.asset = asset;
        if (note != null && asset != null) {
            this.id = new NoteDocsId(note.getId(), asset.getId());
        }
    }
}
