package com.scripto.backend.tag.repository;

import com.scripto.backend.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNormalizedName(String normalizedName);

    @Modifying
    @Query(value = "INSERT IGNORE INTO tags(name, normalized_name) VALUES (:name, :normalizedName)", nativeQuery = true)
    int insertIgnore(@Param("name") String name, @Param("normalizedName") String normalizedName);
}
