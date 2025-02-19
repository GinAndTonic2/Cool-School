package com.coolSchool.CoolSchool.services.impl;

import com.coolSchool.CoolSchool.enums.FileType;
import com.coolSchool.CoolSchool.exceptions.common.InternalServerErrorException;
import com.coolSchool.CoolSchool.exceptions.files.DirectoryCreationException;
import com.coolSchool.CoolSchool.exceptions.files.FileNotFoundException;
import com.coolSchool.CoolSchool.exceptions.files.UnsupportedFileTypeException;
import com.coolSchool.CoolSchool.models.entity.File;
import com.coolSchool.CoolSchool.repositories.FileRepository;
import com.coolSchool.CoolSchool.services.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import static org.codehaus.plexus.util.FileUtils.getExtension;

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    @Value("${upload.directory}")
    public String uploadDirectory;

    public FileServiceImpl(FileRepository fileRepository, @Value("${upload.directory}") String uploadDirectory) {
        this.fileRepository = fileRepository;
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    @Override
    public Path createFilePath(String uniqueFilename) {
        Path directoryPath = Paths.get(uploadDirectory);
        try {
            Files.createDirectories(directoryPath);
            return directoryPath.resolve(uniqueFilename);
        } catch (IOException e) {
            throw new DirectoryCreationException();
        }
    }

    @Override
    public File uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String extension = getExtension(originalFilename);

            if (!FileType.isSupportedExtension(extension)) {
                throw new UnsupportedFileTypeException();
            }

            String uniqueFilename = saveFileAndGetUniqueFilename(file);
            File fileEntity = new File();
            fileEntity.setName(originalFilename);
            fileEntity.setUrl(uploadDirectory + uniqueFilename);
            fileEntity.setDeleted(false);
            fileEntity.setType(file.getContentType());

            return fileRepository.save(fileEntity);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid argument.", e);
        }
    }

    @Override
    public byte[] getFileBytes(String imageName) {
        try {
            return Files.readAllBytes(Paths.get(uploadDirectory, imageName));
        } catch (NoSuchFileException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    @Override
    public String saveFileAndGetUniqueFilename(MultipartFile file) {
        try {
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            Files.copy(file.getInputStream(), createFilePath(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename;
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    @Override
    public MediaType getMediaTypeForFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return FileType.getMediaTypeForExtension(extension);
    }

}
