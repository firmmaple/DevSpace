package org.jeffrey.service.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 * 处理文件上传、存储和访问等通用功能
 */
public interface FileService {
    
    /**
     * 上传文件到指定目录
     *
     * @param file 上传的文件
     * @param directory 存储目录（相对路径）
     * @param allowedTypes 允许的文件类型（MIME类型，如"image/jpeg"）
     * @param maxSize 最大文件大小（字节）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String directory, String[] allowedTypes, long maxSize);
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 获取文件的绝对路径
     *
     * @param fileUrl 文件访问URL
     * @return 文件的绝对路径
     */
    String getFilePath(String fileUrl);
    
    /**
     * 从URL中提取文件名
     *
     * @param fileUrl 文件访问URL
     * @return 文件名
     */
    String extractFilename(String fileUrl);
    
    /**
     * 获取文件服务的基础URL
     *
     * @return 基础URL
     */
    String getBaseUrl();
} 