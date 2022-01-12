package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import io.ran.TypeDescriberImpl;
import io.ran.token.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SqlGeneratorMariaDbIT extends SqlGeneratorITBase {


	@Override
	protected DataSource getDataSource() {
		return MariaDbDataSourceProvider.get();
	}



}