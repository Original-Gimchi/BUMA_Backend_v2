package com.project.bumawiki.domain.docs.presentation.dto.request;

import com.project.bumawiki.domain.docs.domain.type.DocsType;

import jakarta.validation.constraints.NotNull;

public record DocsTypeUpdateDto(
	@NotNull
	Long id,
	@NotNull
	DocsType docsType
) {
}
