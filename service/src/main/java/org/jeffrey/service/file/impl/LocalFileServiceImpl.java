package org.jeffrey.service.file.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeffrey.service.file.FileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@Service
public class LocalFileServiceImpl implements FileService {
    
    private static final String BASE_UPLOAD_DIR = "uploads";
    private static final String API_URL_PREFIX = "/api/file";
    
    @Override
    public String uploadFile(MultipartFile file, String directory, String[] allowedTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            return null;
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType, allowedTypes)) {
            log.warn("不支持的文件类型: {}", contentType);
            return null;
        }
        
        // 检查文件大小
        if (file.getSize() > maxSize) {
            log.warn("文件过大: {}，最大允许: {}", file.getSize(), maxSize);
            return null;
        }
        
        try {
            // 确保上传目录存在
            String uploadDir = BASE_UPLOAD_DIR + "/" + directory;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID() + "." + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件访问URL
            return String.format("%s/%s/%s", API_URL_PREFIX, directory, newFilename);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return null;
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(getFilePath(fileUrl));
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public String getFilePath(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(API_URL_PREFIX)) {
            return null;
        }
        
        // 从URL中提取相对路径
        String relativePath = fileUrl.substring(API_URL_PREFIX.length());
        return BASE_UPLOAD_DIR + relativePath;
    }
    
    @Override
    public String extractFilename(String fileUrl) {
        if (fileUrl == null) return null;
        
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex > 0 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        return null;
    }
    
    /**
     * 检查文件类型是否被允许
     */
    private boolean isAllowedType(String contentType, String[] allowedTypes) {
        return Arrays.asList(allowedTypes).contains(contentType);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
} 