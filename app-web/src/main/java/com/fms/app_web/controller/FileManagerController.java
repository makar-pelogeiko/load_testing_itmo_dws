package com.fms.app_web.controller;

import com.fms.app_web.dto.FileRequestDTO;
import com.fms.app_web.dto.FileResponseDTO;
import com.fms.app_web.dto.ResponseWrapper;
import com.fms.app_web.exception.NameAlreadyExistsException;
import com.fms.app_web.model.File;
import com.fms.app_web.service.FileManagerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class FileManagerController {

    private final FileManagerService fileManagerService;

    @CrossOrigin
    @GetMapping("/t")
    public String tMethod() {
        return "simple letter";
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<ResponseWrapper<FileResponseDTO>> getFileById(@PathVariable long id) {
        ResponseWrapper<FileResponseDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setSuccess(true);
        Optional<File> optional = fileManagerService.getFileById(id);
        if (optional.isEmpty()) {
            return new ResponseEntity<>(responseWrapper, HttpStatus.NOT_FOUND);
        }
        File file = optional.get();
        responseWrapper.setData(new FileResponseDTO(file.getId(), file.getName(), file.getData()));
        return ResponseEntity.ok(responseWrapper);
    }

    @PostMapping("/file")
    public ResponseEntity<ResponseWrapper<FileResponseDTO>> postFile(@RequestBody FileRequestDTO file) {
        ResponseWrapper<FileResponseDTO> response = new ResponseWrapper<>();
        ResponseWrapper<File> savedResult;
        try {
            savedResult = fileManagerService.addNewFile(file);
        } catch (NameAlreadyExistsException e) {
            FileResponseDTO result = new FileResponseDTO(e.getId(), e.getFileName(), null);
            response.setData(result);
            response.setSuccess(false);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        FileResponseDTO result = new FileResponseDTO(savedResult.getData().getId(),
                savedResult.getData().getName(), savedResult.getData().getData());
        response.setData(result);
        response.setSuccess(savedResult.isSuccess());
        response.setComment(savedResult.getComment());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
