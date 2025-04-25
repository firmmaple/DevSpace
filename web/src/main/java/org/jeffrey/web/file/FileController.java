package org.jeffrey.web.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.file.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${app.file.avatar-dir:avatars}")
    private String avatarDir;

    /**
     * 通用文件访问端点
     */
    @GetMapping("/{directory}/{filename:.+}")
    @TraceLog("获取文件")
    public ResponseEntity<Resource> getFile(
            @PathVariable String directory,
            @PathVariable String filename) {

        try {
            // 使用FileService获取文件路径
            String filePath = fileService.getFilePath(fileService.getBaseUrl() + "/" + directory + "/" + filename);
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                // 根据文件扩展名确定内容类型
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("文件URL格式错误", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 头像访问专用端点
     */
    @GetMapping("/${app.file.avatar-dir:avatars}/{filename:.+}")
    @TraceLog("获取用户头像")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        return getFile(avatarDir, filename);
    }

    /**
     * 根据文件名确定内容类型
     */
    private String determineContentType(String filename) {
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (filename.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }
} 