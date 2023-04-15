package com.project.bumawiki.domain.docs.facade;

import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.docs.domain.repository.DocsRepository;
import com.project.bumawiki.domain.docs.exception.DocsNotFoundException;
import com.project.bumawiki.domain.docs.exception.DocsTitleAlreadyExistException;
import com.project.bumawiki.domain.docs.presentation.dto.DocsCreateRequestDto;
import com.project.bumawiki.global.error.exception.BumawikiException;
import com.project.bumawiki.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocsFacade {
    private final DocsRepository docsRepository;

    public Docs findById(Long id, ErrorCode errorCode) {
        return docsRepository
                .findById(id)
                .orElseThrow(() -> new BumawikiException(errorCode));
    }

    public void checkTitleAlreadyExist(String title) {
        Optional<Docs> byTitle = docsRepository.findByTitle(title);
        if (byTitle.isPresent()) throw DocsTitleAlreadyExistException.EXCEPTION;
    }

    ;

    @Transactional
    public Docs createDocs(final DocsCreateRequestDto docsCreateRequestDto) {
        return docsRepository.save(
                Docs.builder()
                        .title(docsCreateRequestDto.getTitle())
                        .enroll(docsCreateRequestDto.getEnroll())
                        .docsType(docsCreateRequestDto.getDocsType())
                        .lastModifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
