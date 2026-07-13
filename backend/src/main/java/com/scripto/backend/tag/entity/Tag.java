package com.scripto.backend.tag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
@EqualsAndHashCode(of = "id")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "tag")
    private List<DocumentTag> documentTags;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}