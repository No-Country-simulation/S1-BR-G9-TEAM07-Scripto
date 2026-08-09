package com.scripto.backend.admin.dto;

import com.scripto.backend.document.domain.Visibility;

public record AdminDocumentUpdateDTO(
        Visibility visibility,
        Boolean blocked
) {
}
