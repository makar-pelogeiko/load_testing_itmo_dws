package com.fms.app_web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fms.app_web.dto.FileRequestDTO;
import com.fms.app_web.dto.ResponseWrapper;
import com.fms.app_web.exception.NameAlreadyExistsException;
import com.fms.app_web.model.File;
import com.fms.app_web.repository.IFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FileManagerService {

    private static final Logger log = LoggerFactory.getLogger(FileManagerService.class);
    private final IFileRepository fileRepository;
    private final ObjectMapper objectMapper;
    private final String ROOT_SAVING_DIR;

    @Autowired
    public FileManagerService(@Value("${save-dir:/var/temp_dir}") String saveDir, IFileRepository fileRepository,
                              ObjectMapper objectMapper) {
        this.ROOT_SAVING_DIR = saveDir;
        this.fileRepository = fileRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<File> getFileById(long id) {
        return fileRepository.findById(id);
    }

    public ResponseWrapper<File> addNewFile(FileRequestDTO f) throws NameAlreadyExistsException {
        Optional<File> searchResult = fileRepository.findByName(f.name());
        if (searchResult.isPresent()) {
            File file = searchResult.get();
            file.setData(f.data());
            file.setSavedOnDisk(false);
            file = fileRepository.save(file);
            fileRepository.flush();
            return new ResponseWrapper<>(true, "reCreated", file);
            // throw new NameAlreadyExistsException(searchResult.get().getId(), f.name(), "file already exists");
        }
        File file = new File();
        file.setName(f.name());
        file.setData(f.data());
        file.setSavedOnDisk(false);
        file = fileRepository.save(file);
        fileRepository.flush();
        return new ResponseWrapper<>(true, "", file);
    }

    @Scheduled(cron = "0 * * * * *")
    public void saveData() {
        log.info("start pushing to local file system");
        List<File> files = fileRepository.findAllBySavedOnDiskIsFalse();
        if (files.isEmpty()) {
            log.info("all files are loaded on disk");
            return;
        }

        Path dirToSave = Paths.get(ROOT_SAVING_DIR);

        if (!Files.exists(dirToSave)) {
            try {
                Files.createDirectories(dirToSave);
            } catch (IOException e) {
                log.error("failed to create dir", e);
                return;
            }
        }
        long files_amount = 0;
        try (Stream<Path> filesInDir = Files.list(dirToSave)) {
            files_amount = filesInDir.count();
        } catch (IOException e) {
            log.error("failed to get list of files in directory for saving", e);
            return;
        }

        String newFileName = "data-" + (files_amount + 1) + ".json";
        Path filePath = dirToSave.resolve(newFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()))) {
            boolean isNotFirst = false;
            for (var file : files) {
                if (!isNotFirst) {
                    isNotFirst = true;

                } else {
                    writer.newLine();
                }
                writer.write(objectMapper.writeValueAsString(file));
            }
        }
        catch (IOException e) {
            log.error("failed to write data to file", e);
        }

        files.forEach(file -> file.setSavedOnDisk(true));
        fileRepository.saveAll(files);
    }
}
