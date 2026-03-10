package sub.boke.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(root);
    }

    public String saveImage(MultipartFile file) throws IOException {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank())
            filename += "." + ext.toLowerCase();
        Path dir = root.resolve("images").resolve(LocalDate.now().toString());
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        Path relative = root.relativize(target);
        return "/" + relative.toString().replace('\\', '/');
    }

    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank()) {
            filename += "." + ext.toLowerCase();
        }

        // 创建目录（按日期分类）
        Path dir = root.resolve(subDirectory).resolve(LocalDate.now().toString());
        Files.createDirectories(dir);

        // 保存文件
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 返回相对路径
        Path relative = root.relativize(target);
        return "/" + relative.toString().replace('\\', '/');
    }
    // 验证文件类型
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    // 验证文件类型（通用）
    public boolean isValidFileType(MultipartFile file, String[] allowedTypes) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        for (String allowedType : allowedTypes) {
            if (contentType.startsWith(allowedType)) {
                return true;
            }
        }
        return false;
    }

    // 获取文件大小限制（5MB）
    public boolean isWithinSizeLimit(MultipartFile file, long maxSizeInBytes) {
        return file.getSize() <= maxSizeInBytes;
    }

}
