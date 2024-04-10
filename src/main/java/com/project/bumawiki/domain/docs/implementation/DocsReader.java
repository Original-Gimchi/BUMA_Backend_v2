package com.project.bumawiki.domain.docs.implementation;

import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.docs.domain.repository.DocsRepository;
import com.project.bumawiki.global.annotation.Implementation;
import com.project.bumawiki.global.error.exception.BumawikiException;
import com.project.bumawiki.global.error.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Implementation
@RequiredArgsConstructor
public class DocsReader {

	private final DocsRepository docsRepository;

	public Docs findById(Long docsId) {
		return docsRepository.findById(docsId)
			.orElseThrow(() -> new BumawikiException(ErrorCode.DOCS_NOT_FOUND));
	}

	public Docs findByTitle(String title) {
		return docsRepository.findByTitle(title)
			.orElseThrow(() -> new BumawikiException(ErrorCode.DOCS_NOT_FOUND));
	}
}
