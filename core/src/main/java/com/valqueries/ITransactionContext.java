/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-26
 */
package com.valqueries;

public interface ITransactionContext extends IOrmOperations, AutoCloseable {
    @Override
    void close();
}
