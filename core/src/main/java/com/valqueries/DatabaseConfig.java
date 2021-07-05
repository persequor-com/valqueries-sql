/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries;

import java.time.Duration;

public class DatabaseConfig {
	/**
	 * Number of maximum retries of a database transaction when a deadlock happens
	 */
	private int defaultRetryCount = 5;
	/**
	 * Wait time between the retries on deadlock exceptions
	 */
	private int defaultRetryWaitMillis = 200;

	public int getDefaultRetryCount() {
		return defaultRetryCount;
	}

	public void setDefaultRetryCount(int defaultRetryCount) {
		this.defaultRetryCount = defaultRetryCount;
	}

	public Duration getDefaultRetryWait() {
		return Duration.ofMillis(defaultRetryWaitMillis);
	}

	public void setDefaultRetryWait(int defaultRetryWaitMillis) {
		this.defaultRetryWaitMillis = defaultRetryWaitMillis;
	}
}
