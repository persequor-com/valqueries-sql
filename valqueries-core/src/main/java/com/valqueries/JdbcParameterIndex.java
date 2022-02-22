/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-11-10
 */
package com.valqueries;

//not thread-safe use AtomicInteger if you need safety
class JdbcParameterIndex {
	//jdbc parameters in java are indexed starting with 1
	private int index = 1;

	public int getAndIncrement(){
		return index++;
	}
}
