package com.scripto.backend.tag.service;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.tag.entity.DocumentTag;
import com.scripto.backend.tag.entity.DocumentTagId;
import com.scripto.backend.tag.entity.Tag;
import com.scripto.backend.tag.repository.DocumentTagRepository;
import com.scripto.backend.tag.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final DocumentTagRepository documentTagRepository;

    public TagService(TagRepository tagRepository, DocumentTagRepository documentTagRepository) {
        this.tagRepository = tagRepository;
        this.documentTagRepository = documentTagRepository;
    }

    @Transactional
    public List<String> attachTags(Document document, List<String> rawTags) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String rawTag : rawTags) {
            String displayName = normalizeDisplayName(rawTag);
            if (!displayName.isBlank()) {
                unique.add(displayName);
            }
            if (unique.size() == 5) {
                break;
            }
        }
        for (String displayName : unique) {
            String normalized = normalizeKey(displayName);
            tagRepository.insertIgnore(displayName, normalized);
            Tag tag = tagRepository.findByNormalizedName(normalized)
                    .orElseThrow(() -> new IllegalStateException("Could not load normalized tag: " + normalized));
            DocumentTagId id = new DocumentTagId(document.getId(), tag.getId());
            if (!documentTagRepository.existsById(id)) {
                documentTagRepository.save(new DocumentTag(id, document, tag));
            }
        }
        return List.copyOf(unique);
    }

    public String normalizeKey(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.]+", " ")
                .strip()
                .replaceAll("\\s+", " ");
    }

    private String normalizeDisplayName(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50).strip();
    }
}
