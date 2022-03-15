package com.valqueries.automapper;

import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.PropertiesColumnizer;
import io.ran.Property;
import io.ran.RelationDescriber;
import io.ran.TestDoubleDb;
import io.ran.TypeDescriberImpl;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDoubleQuery<T> extends io.ran.TestDoubleQuery<T, ValqueriesQuery<T>> implements ValqueriesQuery<T>, ValqueriesGroupQuery<T> {
	private MappingHelper mappingHelper;
	private TestDoubleDb testDoubleDb;
	private GenericFactory factory;
	private ArrayList<Property> groupByProperties;
	private boolean forcedEmpty = false;

	public TestDoubleQuery(Class<T> modelType, GenericFactory genericFactory, MappingHelper mappingHelper, TestDoubleDb testDoubleDb) {
		super(modelType, genericFactory, mappingHelper, testDoubleDb);
		this.mappingHelper = mappingHelper;
		this.testDoubleDb = testDoubleDb;
		this.factory = genericFactory;
	}

	public ValqueriesQuery<T> eq(Property.PropertyValue<?> propertyValue) {
		filters.add(t -> {
			Object actualValue = getValue(propertyValue.getProperty(), t);
			return Objects.equals(actualValue, propertyValue.getValue());
		});
		return this;
	}

	public ValqueriesQuery<T> gt(Property.PropertyValue<?> propertyValue) {
		filters.add(t -> {
			Object actualValue = getValue(propertyValue.getProperty(), t);
			if (actualValue instanceof Comparable) {
				return ((Comparable) actualValue).compareTo(propertyValue.getValue()) > 0;
			}
			return false;
		});
		return this;
	}

	public ValqueriesQuery<T> lt(Property.PropertyValue<?> propertyValue) {
		filters.add(t -> {
			Object actualValue = getValue(propertyValue.getProperty(), t);
			if (actualValue instanceof Comparable) {
				return ((Comparable) actualValue).compareTo(propertyValue.getValue()) < 0;
			}
			return false;
		});
		return this;
	}

	public ValqueriesQuery<T> isNull(Property<?> property) {
		filters.add(t -> {
			Object actualValue = getValue(property, t);
			return actualValue == null;
		});
		return this;
	}

	public ValqueriesQuery<T> isNotNull(Property<?> property) {
		filters.add(t -> {
			Object actualValue = getValue(property, t);
			return actualValue != null;
		});
		return this;
	}

	public ValqueriesQuery<T> in(Property.PropertyValueList propertyValues) {
		filters.add(t -> {
			if (propertyValues.isEmpty()) {
				this.forcedEmpty = true;
			}
			Object actualValue = getValue(propertyValues.getProperty(), t);
			return ((Property.PropertyValueList<?>)propertyValues).stream().anyMatch(pv -> Objects.equals(actualValue, pv.getValue()));
		});
		return this;
	}

	public ValqueriesQuery<T> like(Property.PropertyValue<?> propertyValue) {
		if (!(propertyValue.getValue() instanceof String)) {
			throw new RuntimeException("LIKE operator only valid for Strings, "+propertyValue.getValue().getClass().getName()+" provided");
		}
		Pattern pattern = Pattern.compile(propertyValue.getValue().toString().replace("%",".*"));

		filters.add(t -> {
			Object actualValue = getValue(propertyValue.getProperty(), t);
			return pattern.matcher(actualValue.toString()).find();
		});
		return this;
	}

	public ValqueriesQuery<T> freetext(Property.PropertyValue<?> propertyValue) {
		if (!(propertyValue.getValue() instanceof String)) {
			throw new RuntimeException("freetext operator only valid for Strings, "+propertyValue.getValue().getClass().getName()+" provided");
		}

		filters.add(t -> {
			Object actualValue = getValue(propertyValue.getProperty(), t);
			return actualValue.toString().contains(((String) propertyValue.getValue()).toString());
		});
		return this;
	}

	@Override
	public ValqueriesQuery<T> withEager(RelationDescriber relationDescriber) {
		// Eager should not be needed to be implemented by test doubles, as they should already be setup on the model
		return this;
	}


	@Override
	public <X, Z extends CrudRepository.InlineQuery<X, Z>> ValqueriesQuery<T> subQuery(RelationDescriber relationDescriber, Consumer<Z> consumer) {
		if (!relationDescriber.getVia().isEmpty()) {
			return subQuery(relationDescriber.getVia().get(0), q -> {
				((TestDoubleQuery<X>)q).subQuery(relationDescriber.getVia().get(1), (Consumer) consumer);
			});
		}
		TestDoubleQuery<X> otherQuery = new TestDoubleQuery(relationDescriber.getToClass().clazz,  factory, mappingHelper, testDoubleDb);
		consumer.accept((Z) otherQuery);

//		return this;
//
//		TestDoubleQuery<X> subQuery = new TestDoubleQuery<X>((Class<X>)relationDescriber.getToClass().clazz, factory, mappingHelper, testDoubleDb);
//		consumer.accept((Z)subQuery);

		filters.add(t -> {
			List<X> subResult = otherQuery.execute().collect(Collectors.toList());
			for(int i=0;i<relationDescriber.getFromKeys().size();i++) {
				Object tv = mappingHelper.getValue(t, relationDescriber.getFromKeys().get(i).getProperty());
				int finalI = i;
				subResult.removeIf(o -> {
					Object ov = mappingHelper.getValue(o, relationDescriber.getToKeys().get(finalI).getProperty());
					return !tv.equals(ov);
				});
			}
			return !subResult.isEmpty();
		});
		return this;


	}

	@Override
	protected ValqueriesQuery<T> getQuery(Class<?> aClass) {
		return new TestDoubleQuery<T>((Class)aClass, factory, mappingHelper, testDoubleDb);
	}


	protected Object getValue(Property<?> property, T t) {
		return mappingHelper.getValue(t, property);
	}


	////


	@Override
	public <X> ValqueriesQuery<T> in(Function<T, X> field, Collection<X> value) {
		field.apply(instance);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(BiConsumer<T, X> field, Collection<X> value) {
		field.accept(instance, null);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(Function<T, X> field, X... value) {
		if (value != null && value.length == 1 && value[0] instanceof Collection) {
			throw new IllegalArgumentException("The type of the values is most probably incorrectly inferred, please make sure field type matches the type of the values array");
		}
		field.apply(instance);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(BiConsumer<T, X> field, X... value) {
		field.accept(instance, null);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(Function<T, String> field, String value) {
		field.apply(instance);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(BiConsumer<T, String> field, String value) {
		field.accept(instance, null);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> freetext(Function<T, String> field, String value) {
		field.apply(instance);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> freetext(BiConsumer<T, String> field, String value) {
		field.accept(instance, null);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNotNull(Function<T, String> field) {
		field.apply(instance);
		isNotNull(queryWrapper.getCurrentProperty());
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNotNull(BiConsumer<T, String> field) {
		field.accept(instance, null);
		isNotNull(queryWrapper.getCurrentProperty());
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQuery(Function<T, X> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.apply(instance);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQuery(BiConsumer<T, X> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.accept(instance, null);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQueryList(Function<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.apply(instance);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQueryList(BiConsumer<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.accept(instance, null);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQueryList(RelationDescriber relationDescriber, Consumer<ValqueriesQuery<X>> subQuery) {
		this.subQuery(relationDescriber, subQuery);
		return this;
	}

	@Override
	public CrudRepository.CrudUpdateResult update(Consumer<ValqueriesUpdate<T>> updater) {
		if (forcedEmpty) {
			return () -> 0;
		}
		List<Property.PropertyValue> newPropertyValues = this.getPropertyValuesFromUpdater(updater);
		List<T> records = execute().collect(Collectors.toList());
		records.forEach(t -> {
			List<Property.PropertyValue> storedValues = this.getStoredValues(t);
			Property.PropertyValueList updatedPropertyValues = new Property.PropertyValueList();
			storedValues.forEach((storedPropertyValue) -> {
				Optional<? extends Property.PropertyValue> propertyValue = this.getNewPropertyValue(storedPropertyValue, newPropertyValues);
				if (propertyValue.isPresent()) {
					updatedPropertyValues.add(propertyValue.get());
				} else {
					updatedPropertyValues.add(storedPropertyValue);
				}
			});
			mappingHelper.hydrate(t, new PropertyValueHydrator(updatedPropertyValues));
		});
		return records::size;
	}

	private List<Property.PropertyValue> getPropertyValuesFromUpdater(Consumer<ValqueriesUpdate<T>> updater) {
		ValqueriesUpdateImpl<T> updImpl = new ValqueriesUpdateImpl(instance, queryWrapper);
		updater.accept(updImpl);
		return updImpl.getPropertyValues();
	}

	private List<Property.PropertyValue> getStoredValues(T t) {
		Property.PropertyList fields = TypeDescriberImpl.getTypeDescriber(clazz).fields();
		PropertiesColumnizer columnizer = new PropertiesColumnizer(fields);
		mappingHelper.columnize(t, columnizer);
		return columnizer.getValues();
	}

	private Optional<? extends Property.PropertyValue> getNewPropertyValue(Property.PropertyValue propertyValue, List<Property.PropertyValue> newPropertyValues) {
		return newPropertyValues.stream()
				.filter(pv -> pv.getProperty().getToken().equals(propertyValue.getProperty().getToken()))
				.findFirst();
	}
	@Override
	public ValqueriesGroupQuery<T> groupBy(Function<T, ?>... field) {
		groupByProperties = new ArrayList<>();
		for (Function<T, ?> f : field) {
			f.apply(instance);
			groupByProperties.add(queryWrapper.getCurrentProperty().copy());
		}
		return this;
	}

	@Override
	public ValqueriesGroupQuery<T> groupBy(BiConsumer<T, ?>... field) {
		groupByProperties = new ArrayList<>();
		for (BiConsumer<T, ?> f : field) {
			f.accept(instance, null);
			groupByProperties.add(queryWrapper.getCurrentProperty().copy());
		}
		return this;
	}

	@Override
	public GroupNumericResult count(Function<T, ?> field) {
		field.apply(instance);
		return count(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult count(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return count(queryWrapper.getCurrentProperty());
	}
	@Override
	public GroupNumericResult sum(Function<T, ?> field) {
		field.apply(instance);
		return sum(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult max(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return max(queryWrapper.getCurrentProperty());
	}

	@Override
	public GroupNumericResult max(Function<T, ?> field) {
		field.apply(instance);
		return max(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult min(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return min(queryWrapper.getCurrentProperty());
	}

	@Override
	public GroupNumericResult min(Function<T, ?> field) {
		field.apply(instance);
		return min(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult sum(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return sum(queryWrapper.getCurrentProperty());
	}

	private GroupNumericResult count(Property property) {
		return aggregateFunction(property, o -> o.stream().distinct().count());
	}

	private GroupNumericResult sum(Property property) {
		return aggregateFunction(property, o -> o.stream().mapToLong(l -> Long.valueOf(l.toString())).sum());
	}

	private GroupNumericResult max(Property property) {
		return aggregateFunction(property, o -> o.stream().mapToLong(l -> Long.valueOf(l.toString())).max().orElse(0));
	}

	private GroupNumericResult min(Property property) {
		return aggregateFunction(property, o -> o.stream().mapToLong(l -> Long.valueOf(l.toString())).min().orElse(0));
	}

	private GroupNumericResult aggregateFunction(Property property, Function<List<Object>,Long> func) {
		return new GroupNumericResultImpl(execute()
			.map(t -> new GroupNumericResultImpl.Grouping(groupByProperties.stream().map(p -> getValue(p, t)).collect(Collectors.toList()), getValue(property, t)))
			.collect(Collectors.toMap(g -> g, g -> new ArrayList(Arrays.asList(g.getValue())), (v1, v2) -> {
				v1.addAll(v2);
				return v1;
			}))
			.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey(), e -> func.apply(e.getValue()))));
	}

	public Stream<T> execute() {
		if (forcedEmpty) {
			return Stream.empty();
		}
		return super.execute();
	}

	public CrudRepository.CrudUpdateResult delete() {
		if (forcedEmpty) {
			return () -> 0;
		}
		return super.delete();
	}
}
