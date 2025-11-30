//package com.cabybara.prolearningplatform.service.upload.impl;
//
//
//import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
//import com.cabybara.prolearningplatform.dto.response.UploadFileResponseDTO;
//import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
//import com.cabybara.prolearningplatform.model.Note;
//import com.cabybara.prolearningplatform.model.NoteDocs;
//import com.cabybara.prolearningplatform.model.NoteImgs;
//import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
//import com.cabybara.prolearningplatform.repository.NoteImgsRepository;
//import com.cabybara.prolearningplatform.repository.NoteRepository;
//import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
//import com.cabybara.prolearningplatform.service.upload.FileConvertService;
//import com.cabybara.prolearningplatform.service.upload.UploadFileService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.multipart.MultipartFile;
//
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.io.IOException;
//
//import java.io.InputStream;
//import java.util.UUID;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class UploadFileServiceImpl implements UploadFileService {
//    private final CloudinaryService cloudinaryService;
//
//    private final NoteRepository noteRepository;
//    private final NoteDocsRepository noteDocsRepository;
//    private final NoteImgsRepository noteImgsRepository;
//
//    private final FileConvertService fileConvertService;
//
//    @Override
//    public UploadFileResponseDTO uploadFile(MultipartFile file, String subject, Long id) throws IOException {
//        // Get file name
//        String fileName = file.getOriginalFilename();
//        log.info("Original filename: {}", fileName);
//
//        // Separate filename and extension
//        String extension = "";
//        int lastDotIndex = fileName.lastIndexOf(".");
//        if (lastDotIndex != -1) {
//            extension = fileName.substring(lastDotIndex + 1);
//            fileName = fileName.substring(0, lastDotIndex);
//        }
//        log.info("File extension: {}", extension);
//
//        // Generate unique filename
//        String uniqueFileName = fileName + "_" + UUID.randomUUID() + "." + extension;
//        log.info("Generated unique filename: {}", uniqueFileName);
//
//        // TODO: Handle convert all file to PDF
////        File pdfFile = fileConvertService.convertToPDF(file, extension);
////        MultipartFile uploadFile = new MultipartFile() {
////            @Override
////            public String getName() {
////                return pdfFile.getName();
////            }
////
////            @Override
////            public String getOriginalFilename() {
////                return pdfFile.getName();
////            }
////
////            @Override
////            public String getContentType() {
////                return "application/pdf";
////            }
////
////            @Override
////            public boolean isEmpty() {
////                return pdfFile.length() == 0;
////            }
////
////            @Override
////            public long getSize() {
////                return pdfFile.length();
////            }
////
////            @Override
////            public byte[] getBytes() throws IOException {
////                return java.nio.file.Files.readAllBytes(pdfFile.toPath());
////            }
////
////            @Override
////            public InputStream getInputStream() throws IOException {
////                return java.nio.file.Files.newInputStream(pdfFile.toPath());
////            }
////
////            @Override
////            public void transferTo(File dest) throws IOException, IllegalStateException {
////                java.nio.file.Files.copy(pdfFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
////            }
////        };
//
////        extension = "pdf"; // đổi extension sang PDF
////        uniqueFileName = fileName + "_" + UUID.randomUUID() + ".pdf";
//
//        // Upload file
//        CloudinaryResponseDTO cldResponse = cloudinaryService.uploadMultipartFile(file, uniqueFileName, extension, subject);
//
//        // Handle return data
//        UploadFileResponseDTO uploadRes = new UploadFileResponseDTO();
//        if (subject.equals("note-document")) {
//            // Convert pdf url to HTML and return content
////            String content = convertFileService.convertToHTML(cldResponse.getFileUrl());
////            log.info("Converted file content: {}", content);
//
//            // Save note document to database
//            NoteDocs savedDocs = saveNoteDocs(uniqueFileName, cldResponse.getFileUrl(), extension, cldResponse.getPublicId(), id);
//            uploadRes = UploadFileResponseDTO.builder()
//                    .id(savedDocs.getId())
//                    .fileName(uniqueFileName)
//                    .fileUrl(savedDocs.getFileUrl())
//                    .extension(extension)
//                    .publicId(savedDocs.getPublicId())
//                    .build();
//        } else if (subject.equals("note-image")) {
//            NoteImgs savedImgs = saveNoteImgs(uniqueFileName, cldResponse.getFileUrl(), extension, cldResponse.getPublicId(), id);
//            uploadRes = UploadFileResponseDTO.builder()
//                    .id(savedImgs.getId())
//                    .fileName(uniqueFileName)
//                    .fileUrl(savedImgs.getFileUrl())
//                    .content("")
//                    .extension(extension)
//                    .publicId(savedImgs.getPublicId())
//                    .build();
//        }
//        return uploadRes;
//    }
//
//    private NoteDocs saveNoteDocs(String fileName, String fileUrl, String extension, String publicId, Long noteId) {
//        Note note = getNoteById(noteId);
//
//        NoteDocs noteDocs = NoteDocs.builder()
//                .fileUrl(fileUrl)
//                .fileName(fileName)
//                .extension(extension)
//                .publicId(publicId)
//                .note(note)
//                .build();
//
//        NoteDocs saved = noteDocsRepository.save(noteDocs);
//        return saved;
//    }
//
//    private NoteImgs saveNoteImgs(String fileName, String fileUrl, String extension, String publicId, Long noteId) {
//        Note note = getNoteById(noteId);
//
//        NoteImgs noteImgs = NoteImgs.builder()
//                .fileUrl(fileUrl)
//                .fileName(fileName)
//                .extension(extension)
//                .publicId(publicId)
//                .note(note)
//                .build();
//        NoteImgs saved = noteImgsRepository.save(noteImgs);
//        return saved;
//    }
//
//    private Note getNoteById(Long noteId) {
//        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
//    }
//}
