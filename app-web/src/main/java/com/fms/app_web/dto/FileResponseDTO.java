package com.fms.app_web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO {
    private long id;
    private String name;
    private String data;

//    public FileResponseDTO(long id, String name, String data) {
//        this.id = id;
//        this.name = name;
//        this.data = data;
//    }
//
//    public FileResponseDTO() {}
}
