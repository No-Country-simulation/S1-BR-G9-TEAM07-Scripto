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

        // Arrange
        Tag javaTag = new Tag();
        javaTag.setId(10L);
        javaTag.setName("java");
        javaTag.setNormalizedName("java");
        when(tagRepository.findByNormalizedName("java")).thenReturn(Optional.of(javaTag));
        when(documentTagRepository.existsById(any())).thenReturn(false);

        Document document = new Document("Title", "Long enough content for the document");
        document.setId(1L);
        TagService service = new TagService(tagRepository, documentTagRepository);

        // Act
        var attached = service.attachTags(document, List.of(" Java ", "java", "JAVA"));

        // Assert
        assertEquals(List.of("java"), attached);
        verify(tagRepository, times(1)).insertIgnore("java", "java");
        verify(documentTagRepository, times(1)).save(any());
    }

    @Test
    void limitsTagsToFive() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        when(documentTagRepository.existsById(any()))
                .thenReturn(false);

        when(tagRepository.findByNormalizedName(anyString()))
                .thenAnswer(invocation -> {
                    String normalizedName = invocation.getArgument(0);

                    Tag tag = new Tag();
                    tag.setId((long) (normalizedName.hashCode()));
                    tag.setName(normalizedName);
                    tag.setNormalizedName(normalizedName);

                    return Optional.of(tag);
                });

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        List<String> tags = List.of(
                "Java",
                "Spring",
                "Backend",
                "REST",
                "API",
                "Docker",
                "PostgreSQL"
        );

        // Act
        var attached = service.attachTags(document, tags);

        // Assert
        assertEquals(
                List.of(
                        "java",
                        "spring",
                        "backend",
                        "rest",
                        "api"
                ),
                attached
        );

        verify(tagRepository, times(5))
                .insertIgnore(anyString(), anyString());

        verify(documentTagRepository, times(5))
                .save(any());
    }

    @Test
    void reusesExistingTag() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        Tag existingTag = new Tag();
        existingTag.setId(10L);
        existingTag.setName("java");
        existingTag.setNormalizedName("java");

        when(tagRepository.findByNormalizedName("java"))
                .thenReturn(Optional.of(existingTag));

        when(documentTagRepository.existsById(any()))
                .thenReturn(false);

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        // Act
        var attached = service.attachTags(
                document,
                List.of("Java")
        );

        // Assert
        assertEquals(List.of("java"), attached);

        verify(tagRepository, times(1))
                .insertIgnore("java", "java");

        verify(tagRepository, times(1))
                .findByNormalizedName("java");

        verify(documentTagRepository, times(1))
                .existsById(any());

        verify(documentTagRepository, times(1))
                .save(any());
    }

    @Test
    void doesNotCreateDuplicateDocumentTag() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        Tag javaTag = new Tag();
        javaTag.setId(10L);
        javaTag.setName("java");
        javaTag.setNormalizedName("java");

        when(tagRepository.findByNormalizedName("java"))
                .thenReturn(Optional.of(javaTag));

        // A tag já está associada ao documento
        when(documentTagRepository.existsById(any()))
                .thenReturn(true);

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        // Act
        var attached = service.attachTags(
                document,
                List.of("Java")
        );

        // Assert
        assertEquals(List.of("java"), attached);

        verify(tagRepository, times(1))
                .insertIgnore("java", "java");

        verify(tagRepository, times(1))
                .findByNormalizedName("java");

        verify(documentTagRepository, times(1))
                .existsById(any());

        verify(documentTagRepository, never())
                .save(any());
    }

    @Test
    void ignoresBlankTags() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        Tag javaTag = new Tag();
        javaTag.setId(10L);
        javaTag.setName("java");
        javaTag.setNormalizedName("java");

        when(tagRepository.findByNormalizedName("java"))
                .thenReturn(Optional.of(javaTag));

        when(documentTagRepository.existsById(any()))
                .thenReturn(false);

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        // Act
        var attached = service.attachTags(
                document,
                List.of("", "   ", "Java")
        );

        // Assert
        assertEquals(List.of("java"), attached);

        verify(tagRepository, times(1))
                .insertIgnore("java", "java");

        verify(documentTagRepository, times(1))
                .save(any());
    }

    @Test
    void limitsTagDisplayNameToFiftyCharacters() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        String longTag = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        String expectedTag = longTag
                .substring(0, 50)
                .toLowerCase();

        Tag tag = new Tag();
        tag.setId(10L);
        tag.setName(expectedTag);
        tag.setNormalizedName(expectedTag);

        when(tagRepository.findByNormalizedName(expectedTag))
                .thenReturn(Optional.of(tag));

        when(documentTagRepository.existsById(any()))
                .thenReturn(false);

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        // Act
        var attached = service.attachTags(
                document,
                List.of(longTag)
        );

        // Assert
        assertEquals(1, attached.size());
        assertEquals(50, attached.get(0).length());
        assertEquals(expectedTag, attached.get(0));

        verify(tagRepository, times(1))
                .insertIgnore(expectedTag, expectedTag);

        verify(documentTagRepository, times(1))
                .save(any());
    }

    @Test
    void preservesTagOrderAfterDeduplication() {

        // Arrange
        TagRepository tagRepository = mock(TagRepository.class);
        DocumentTagRepository documentTagRepository = mock(DocumentTagRepository.class);

        when(documentTagRepository.existsById(any()))
                .thenReturn(false);

        when(tagRepository.findByNormalizedName(anyString()))
                .thenAnswer(invocation -> {
                    String normalizedName = invocation.getArgument(0);

                    Tag tag = new Tag();
                    tag.setId((long) normalizedName.hashCode());
                    tag.setName(normalizedName);
                    tag.setNormalizedName(normalizedName);

                    return Optional.of(tag);
                });

        Document document = new Document(
                "Title",
                "Long enough content for the document"
        );

        document.setId(1L);

        TagService service = new TagService(
                tagRepository,
                documentTagRepository
        );

        // Act
        var attached = service.attachTags(
                document,
                List.of(
                        "Spring",
                        "Java",
                        "Backend",
                        "Java",
                        "Spring"
                )
        );

        // Assert
        assertEquals(
                List.of(
                        "spring",
                        "java",
                        "backend"
                ),
                attached
        );

        verify(tagRepository, times(3))
                .insertIgnore(anyString(), anyString());

        verify(documentTagRepository, times(3))
                .save(any());
    }


}
