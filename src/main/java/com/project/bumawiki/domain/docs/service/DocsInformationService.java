package com.project.bumawiki.domain.docs.service;

import static org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.docs.domain.VersionDocs;
import com.project.bumawiki.domain.docs.domain.repository.DocsRepository;
import com.project.bumawiki.domain.docs.domain.repository.VersionDocsRepository;
import com.project.bumawiki.domain.docs.domain.type.DocsType;
import com.project.bumawiki.domain.docs.exception.DocsNotFoundException;
import com.project.bumawiki.domain.docs.exception.VersionNotExistException;
import com.project.bumawiki.domain.docs.presentation.dto.ClubResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.TeacherResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.VersionDocsSummaryDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.DocsNameAndEnrollResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.DocsResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionDocsDiffResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionResponseDto;
import com.project.bumawiki.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DocsInformationService {
	private final DocsRepository docsRepository;
	private final VersionDocsRepository versionDocsRepository;

	public List<DocsNameAndEnrollResponseDto> findAllByTitle(String title) {
		List<Docs> docs = docsRepository.findAllByTitle(title);

		if (docs.isEmpty()) {
			throw DocsNotFoundException.EXCEPTION;
		}

		return docs.stream()
			.map(DocsNameAndEnrollResponseDto::new)
			.collect(Collectors.toList());
	}

	public DocsResponseDto findDocs(String title) {
		Docs docs = docsRepository.findByTitle(title)
			.orElseThrow(() -> DocsNotFoundException.EXCEPTION);

		List<User> contributors = versionDocsRepository.findByDocs(docs)
			.stream().map(VersionDocs::getContributor)
			.toList();

		return new DocsResponseDto(docs, contributors);
	}

	public VersionResponseDto findDocsVersion(String title) {
		Docs docs = docsRepository.findByTitle(title)
			.orElseThrow(() -> DocsNotFoundException.EXCEPTION);

		return docsRepository.getDocsVersion(docs);
	}

	public List<DocsNameAndEnrollResponseDto> showDocsModifiedAtDesc(Pageable pageable) {
		return docsRepository.findByLastModifiedAt(pageable)
			.stream()
			.map(DocsNameAndEnrollResponseDto::new)
			.collect(Collectors.toList());
	}

	public List<DocsNameAndEnrollResponseDto> showDocsModifiedAtDescAll() {
		return docsRepository.findByLastModifiedAtAll()
			.stream()
			.map(DocsNameAndEnrollResponseDto::new)
			.collect(Collectors.toList());
	}

	public VersionDocsDiffResponseDto showVersionDocsDiff(String title, Long version) {
		Docs docs = docsRepository.findByTitle(title).orElseThrow(
			() -> DocsNotFoundException.EXCEPTION
		);
		String baseDocs = "";
		String versionedDocs;
		List<VersionDocs> versionDocs = docs.getDocsVersion();
		try {
			versionedDocs = versionDocs.get(version.intValue()).getContents();
			if (version > 0) {
				baseDocs = versionDocs.get((int)(version - 1)).getContents();
			}
		} catch (IndexOutOfBoundsException e) {
			throw VersionNotExistException.EXCEPTION;
		}

		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<Diff> diff = dmp.diffMain(baseDocs, versionedDocs);
		dmp.diffCleanupSemantic(diff);

		return new VersionDocsDiffResponseDto(docs.getTitle(), docs.getDocsType(),
			new VersionDocsSummaryDto(versionDocs.get(version.intValue())), new ArrayList<>(diff));
	}

	//Docs Type으로 조회
	public List<Docs> findByDocsTypeOrderByEnroll(DocsType docsType) {
		return docsRepository.findByDocsType(docsType);
	}

	public TeacherResponseDto getAllTeacher() {
		return new TeacherResponseDto(
			findByDocsType(DocsType.TEACHER),
			findByDocsType(DocsType.MAJOR_TEACHER),
			findByDocsType(DocsType.MENTOR_TEACHER)
		);
	}

	public ClubResponseDto getAllClub() {
		return new ClubResponseDto(
			findByDocsType(DocsType.CLUB),
			findByDocsType(DocsType.FREE_CLUB)
		);
	}

	private List<Docs> findByDocsType(DocsType docsType) {
		return docsRepository.findByDocsType(docsType);
	}

}
