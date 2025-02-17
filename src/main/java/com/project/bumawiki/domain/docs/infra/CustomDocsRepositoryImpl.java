package com.project.bumawiki.domain.docs.infra;

import static com.project.bumawiki.domain.docs.domain.QDocs.*;
import static com.project.bumawiki.domain.docs.domain.QVersionDocs.*;
import static com.project.bumawiki.domain.thumbsup.domain.QThumbsUp.*;
import static com.project.bumawiki.domain.user.domain.QUser.*;
import static com.querydsl.core.types.Projections.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.docs.domain.repository.CustomDocsRepository;
import com.project.bumawiki.domain.docs.presentation.dto.response.DocsPopularResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionDocsResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CustomDocsRepositoryImpl implements CustomDocsRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public VersionResponseDto getDocsVersion(Docs findDocs) {
		List<VersionDocsResponseDto> versionDocsResponseDto = jpaQueryFactory
			.select(constructor(VersionDocsResponseDto.class, versionDocs.createdAt, user.id,
				user.nickName, versionDocs.version))
			.from(docs)
			.join(docs.versionDocs, versionDocs)
			.join(versionDocs.user, user)
			.where(docs.id.eq(findDocs.getId()))
			.distinct()
			.orderBy(versionDocs.createdAt.desc())
			.fetch();

		return new VersionResponseDto(versionDocsResponseDto, findDocs);
	}

	@Override
	public List<DocsPopularResponseDto> findByThumbsUpsDesc() {
		return jpaQueryFactory
			.select(
				constructor(DocsPopularResponseDto.class, thumbsUp.docs.title, thumbsUp.docs.enroll,
					thumbsUp.docs.docsType, thumbsUp.id.count()))
			.from(thumbsUp)
			.innerJoin(thumbsUp.docs)
			.groupBy(thumbsUp.docs.title, thumbsUp.docs.enroll, thumbsUp.docs.docsType)
			.orderBy(thumbsUp.id.count().desc())
			.limit(25)
			.fetch();
	}
}
