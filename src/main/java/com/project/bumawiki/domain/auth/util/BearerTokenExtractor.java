package com.project.bumawiki.domain.auth.util;

import com.project.bumawiki.global.error.exception.BumawikiException;
import com.project.bumawiki.global.error.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BearerTokenExtractor {

	private static final String BEARER_TYPE = "Bearer ";
	private static final String BEARER_JWT_REGEX = "^Bearer [A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

	public static String extract(String bearer) {
		validate(bearer);
		return bearer.replace(BEARER_TYPE, "").trim();
	}

	private static void validate(String authorization) {
		if (authorization == null) {
			throw new BumawikiException(ErrorCode.TOKEN_MISSING);
		}
		if (!authorization.matches(BEARER_JWT_REGEX)) {
			throw new BumawikiException(ErrorCode.INVALID_JWT);
		}
	}

}
