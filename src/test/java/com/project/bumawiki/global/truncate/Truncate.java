package com.project.bumawiki.global.truncate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Truncate {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	public void beforeEach() {
		final List<String> truncateQueries = getTruncateQueries(jdbcTemplate);
		execute(jdbcTemplate, "SET REFERENTIAL_INTEGRITY FALSE");
		truncateTables(jdbcTemplate, truncateQueries);
		execute(jdbcTemplate, "SET REFERENTIAL_INTEGRITY TRUE");
	}

	private List<String> getTruncateQueries(final JdbcTemplate jdbcTemplate) {
		return jdbcTemplate.queryForList(
			"""
				SELECT Concat('TRUNCATE TABLE ', TABLE_NAME, ' RESTART IDENTITY;') AS q
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_SCHEMA = 'PUBLIC'""",
			String.class);
	}

	private void truncateTables(final JdbcTemplate jdbcTemplate, final List<String> truncateQueries) {
		truncateQueries.forEach(v -> execute(jdbcTemplate, v));
	}

	private void execute(final JdbcTemplate jdbcTemplate, final String query) {
		jdbcTemplate.execute(query);
	}
}
