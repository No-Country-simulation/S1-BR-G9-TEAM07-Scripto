package com.scripto.backend.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DocumentTagId implements Serializable {

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "tag_id")
    private Long tagId;
}