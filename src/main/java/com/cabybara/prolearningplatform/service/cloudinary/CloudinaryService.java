package com.cabybara.prolearningplatform.service.cloudinary;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cloudinary.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CloudinaryService {
    Configuration getConfiguration();

    Map generateUploadSignature(AssetType type);

    Map uploadResourceFromUrl(String url, AssetType type) throws IOException;

    void deleteAssets(List<AssetToDeleteDto> assetToDeleteDtos) throws Exception;

    public CloudinaryResponseDTO uploadMultipartFile(MultipartFile file, String fileName, String extension, String subject) throws IOException;

    public void deleteFile(String publicId, String type) throws IOException;
}