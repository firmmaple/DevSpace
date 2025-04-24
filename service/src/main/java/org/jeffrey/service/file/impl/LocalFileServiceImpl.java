package org.jeffrey.service.file.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.exception.ResourceNotFoundException;
import org.jeffrey.service.file.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/**
 * 本地文件服务实现
 * 将文件存储在服务器本地目录中
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:8080/api/file}")
    private String baseUrl;

    @Override
    public String uploadFile(MultipartFile file, String directory, String[] allowedTypes, long maxSize) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes).contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 生成唯一文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = UUID.randomUUID().toString() + extension;

        // 创建目标目录
        Path uploadPath = Paths.get(uploadDir, directory);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            try (InputStream inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(filename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // 返回文件访问URL
                return baseUrl + "/" + directory + "/" + filename;
            }
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String filePath = getFilePath(fileUrl);
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getFilePath(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            throw new ResourceNotFoundException("无效的文件URL: " + fileUrl);
        }
        
        String relativePath = fileUrl.substring(baseUrl.length());
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        
        return uploadDir + "/" + relativePath;
    }

    @Override
    public String extractFilename(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        String path = fileUrl;
        if (path.contains("/")) {
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        
        return path;
    }
} 