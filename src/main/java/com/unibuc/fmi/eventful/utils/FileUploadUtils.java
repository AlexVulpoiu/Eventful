package com.unibuc.fmi.eventful.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileUploadUtils {

    public static void uploadFile(String uploadDirectory, String fileName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDirectory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectory(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not upload image!");
        }
    }
}
