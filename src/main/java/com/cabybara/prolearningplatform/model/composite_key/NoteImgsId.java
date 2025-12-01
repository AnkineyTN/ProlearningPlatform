package com.cabybara.prolearningplatform.model.composite_key;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteImgsId implements Serializable {
    private Long noteId;
    private Long assetId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteImgsId)) return false;
        NoteImgsId that = (NoteImgsId) o;
        return Objects.equals(noteId, that.noteId) &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, assetId);
    }
}
