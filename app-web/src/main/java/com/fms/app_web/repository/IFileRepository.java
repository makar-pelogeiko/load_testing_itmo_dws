package com.fms.app_web.repository;

import com.fms.app_web.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFileRepository extends JpaRepository<File, Long> {

    Optional<File> findByName(String name);

    List<File> findAllBySavedOnDiskIsFalse();
}
