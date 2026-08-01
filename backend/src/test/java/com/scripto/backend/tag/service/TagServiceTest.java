package com.scripto.backend.tag.service;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.tag.entity.Tag;
import com.scripto.backend.tag.repository.DocumentTagRepository;
import com.scripto.backend.tag.repository.TagRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TagServiceTest {
    @Test
    void normalizesAndDeduplicatesTags() {
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);
        Tag javaTag = new Tag();
        javaTag.setId(10L);
        javaTag.setName("java");
        javaTag.setNormalizedName("java");
        when(tagRepository.findByNormalizedName("java")).thenReturn(Optional.of(javaTag));
        when(documentTagRepository.existsById(any())).thenReturn(false);

        Document document = new Document("Title", "Long enough content for the document");
        document.setId(1L);
        TagService service = new TagService(tagRepository, documentTagRepository);

        var attached = service.attachTags(document, List.of(" Java ", "java", "JAVA"));

        assertEquals(List.of("java"), attached);
        verify(tagRepository, times(1)).insertIgnore("java", "java");
        verify(documentTagRepository, times(1)).save(any());
    }
}
