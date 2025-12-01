package com.cabybara.prolearningplatform.model.composite_key;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteDocsId implements Serializable {
    private Long noteId;
    private Long assetId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteDocsId)) return false;
        NoteDocsId that = (NoteDocsId) o;
        return Objects.equals(noteId, that.noteId) &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, assetId);
    }
}
