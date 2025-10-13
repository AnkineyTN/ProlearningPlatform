package com.cabybara.prolearningplatform.service.impl;


import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.dto.response.UploadFileResponseDTO;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Note;
import com.cabybara.prolearningplatform.model.NoteDocs;
import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.service.CloudinaryService;
import com.cabybara.prolearningplatform.service.UploadFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadFileServiceImpl implements UploadFileService {
    private final CloudinaryService cloudinaryService;

    private final NoteRepository noteRepository;

    private final NoteDocsRepository noteDocsRepository;

    @Override
    public UploadFileResponseDTO uploadDocument(MultipartFile file, String subject, Long noteId) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Original filename: {}", fileName);
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("File name is invalid");
        }

        String extension = "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            extension = fileName.substring(lastDotIndex + 1);
        }
        log.info("File extension: {}", extension);

        CloudinaryResponseDTO cldResponse = cloudinaryService.uploadMultipartFile(file, fileName, extension, subject);

        UploadFileResponseDTO uploadRes = new UploadFileResponseDTO();
        if (subject.equals("note")) {
            NoteDocs savedDocs = saveNoteDocs(fileName, cldResponse.getFileUrl(), extension, cldResponse.getPublicId(), noteId);
            uploadRes = UploadFileResponseDTO.builder()
                    .id(savedDocs.getId())
                    .fileName(fileName)
                    .fileUrl(savedDocs.getFileUrl())
                    .extension(extension)
                    .publicId(savedDocs.getPublicId())
                    .build();
        }
        return uploadRes;
    }

    private NoteDocs saveNoteDocs(String fileName, String fileUrl, String extension, String publicId, Long noteId) {
        Note note = getNoteById(noteId);

        NoteDocs noteDocs = NoteDocs.builder()
                .fileUrl(fileUrl)
                .fileName(fileName)
                .extension(extension)
                .publicId(publicId)
                .note(note)
                .build();

        NoteDocs saved = noteDocsRepository.save(noteDocs);
        return saved;
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }
}
