package com.valqueries.automapper;

import io.ran.token.Token;

public class SqlNameFormatter {
	public String table(Token key) {
		return key.snake_case();
	}

	public String column(Token key) {
		return key.snake_case();
	}
}
