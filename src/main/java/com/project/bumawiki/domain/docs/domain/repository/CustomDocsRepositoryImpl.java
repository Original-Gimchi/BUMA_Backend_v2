package com.project.bumawiki.domain.docs.domain.repository;

import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.docs.presentation.dto.response.DocsPopularResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionDocsResponseDto;
import com.project.bumawiki.domain.docs.presentation.dto.response.VersionResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.project.bumawiki.domain.contribute.domain.QContribute.contribute;
import static com.project.bumawiki.domain.docs.domain.QDocs.docs;
import static com.project.bumawiki.domain.docs.domain.QVersionDocs.versionDocs;
import static com.project.bumawiki.domain.thumbsUp.domain.QThumbsUp.thumbsUp;
import static com.project.bumawiki.domain.user.domain.QUser.user;
import static com.querydsl.core.types.Projections.constructor;

@RequiredArgsConstructor
@Repository
public class CustomDocsRepositoryImpl implements CustomDocsRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public VersionResponseDto getDocsVersion(Docs findDocs) {
        List<VersionDocsResponseDto> versionDocsResponseDto = jpaQueryFactory
                .select(constructor(VersionDocsResponseDto.class, versionDocs.thisVersionCreatedAt, user.id, user.nickName))
                .from(docs)
                .join(docs.docsVersion, versionDocs)
                .join(versionDocs.contributor, contribute)
                .join(contribute.contributor, user)
                .where(docs.id.eq(findDocs.getId()))
                .distinct()
                .orderBy(versionDocs.thisVersionCreatedAt.desc())
                .fetch();

        return new VersionResponseDto(versionDocsResponseDto);
    }

    @Override
    public List<DocsPopularResponseDto> findByThumbsUpsDesc() {
        return jpaQueryFactory
                .select(constructor(DocsPopularResponseDto.class, docs.title, docs.enroll, docs.docsType, thumbsUp.id.count()))
                .from(docs)
                .innerJoin(docs.docsThumbsUp.thumbsUps, thumbsUp)
                .groupBy(docs.title, docs.enroll, docs.docsType)
                .orderBy(thumbsUp.id.count().desc())
                .limit(25)
                .fetch();

    }
}
