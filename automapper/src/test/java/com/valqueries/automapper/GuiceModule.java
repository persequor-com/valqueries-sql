/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.valqueries.Database;
import io.ran.AutoMapper;
import io.ran.Clazz;
import io.ran.DbResolver;
import io.ran.DbType;
import io.ran.GenericBinder;
import io.ran.GenericFactory;
import io.ran.Resolver;
import io.ran.ResolverImpl;

import java.util.HashMap;
import java.util.Map;

public class GuiceModule extends AbstractModule implements GenericBinder {
	private Map<Class<?>, Class<?>> registeredBindings = new HashMap<>();
	private Database database;
	private Class<? extends DbResolver<Valqueries>> resolverClass;

	public GuiceModule(Database database, Class<? extends DbResolver<Valqueries>> resolverClass) {
		this.database = database;
		this.resolverClass = resolverClass;
	}

	@Override
	public void registerBinding(Class<?> from, Class<?> to) {
		registeredBindings.put(from, to);
	}

	private <T> void b(Class<T> from, Class<?> to) {
		bind(from).to((Class<? extends T>)to);
	}

	@Override
	protected void configure() {
		if (database != null) {
			bind(Database.class).toInstance(database);
		}
		bind(Resolver.class).to(ResolverImpl.class);
		bind(GenericFactory.class).to(GuiceGenericFactory.class);
		bind(new TypeLiteral<DbResolver<Valqueries>>(){}).to(resolverClass);
		registeredBindings.forEach(this::b);
	}

	public static class GuiceGenericFactory implements GenericFactory {
		private Injector injector;

		@Inject
		public GuiceGenericFactory(Injector injector) {
			this.injector = injector;
		}

		@Override
		public <T> T get(Class<T> clazz) {
			return injector.getInstance(AutoMapper.get(clazz));
		}

		@Override
		public <T> T getQueryInstance(Class<T> clazz) {
			return injector.getInstance(AutoMapper.getQueryMaps(clazz));
		}

		@Override
		public DbResolver<DbType> getResolver(Class<? extends DbType> dbTypeClass) {
			Clazz<DbResolver<DbType>> clazz = Clazz.ofClazzes(DbResolver.class, Clazz.of(dbTypeClass));
			return (DbResolver<DbType>) injector.getInstance(Key.get(clazz.getType()));
		}

	}
}
