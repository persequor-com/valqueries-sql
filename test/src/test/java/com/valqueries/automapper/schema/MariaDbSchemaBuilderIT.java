package com.valqueries.automapper.schema;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.OrmException;
import com.valqueries.automapper.GuiceModule;
import com.valqueries.automapper.ValqueriesResolver;
import io.ran.GenericFactory;
import io.ran.token.Token;
import org.graalvm.compiler.lir.SwitchStrategy;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MariaDbSchemaBuilderIT extends BaseSchemaBuilderIT {

	@Override
	protected Database database() {
		return new Database(MariaDbDataSourceProvider.get());
	}
}
