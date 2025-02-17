package com.project.bumawiki.domain.docs.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.project.bumawiki.domain.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@IdClass(VersionDocsPk.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VersionDocs {

	@Id
	@NotNull
	@PositiveOrZero
	private Integer version;

	@Id
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "docs_id")
	private Docs docs;

	@NotNull
	@Column(columnDefinition = "TEXT", nullable = false)
	private String contents;

	@CreatedDate
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;

	public VersionDocs(int version, Docs docs, String contents, User user) {
		this.version = version;
		this.docs = docs;
		this.contents = contents;
		this.user = user;
	}
}
