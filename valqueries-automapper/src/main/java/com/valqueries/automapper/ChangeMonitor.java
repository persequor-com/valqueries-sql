package com.valqueries.automapper;

import io.ran.MappingHelper;

import javax.inject.Inject;
import java.util.*;

public class ChangeMonitor {
	int changedRows = 0;
	Set<Object> alreadySaved = new HashSet<>();
	private MappingHelper mappingHelper;

	public ChangeMonitor(MappingHelper mappingHelper) {
		this.mappingHelper = mappingHelper;
	}

	public int increment(Object object, int changedRows) {
//		int identity = System.identityHashCode(object);
		alreadySaved.add(mappingHelper.getKey(object));
		return this.changedRows += changedRows;
	}

	public int incrementCollection(Collection<?> objects, int changedRows) {
//		int identity = System.identityHashCode(object);
		objects.forEach(t -> {
			alreadySaved.add(getKey(t));
		});

		return this.changedRows += changedRows;
	}

	public int getNumberOfChangedRows() {
		return changedRows;
	}

	public boolean isAlreadySaved(Object t) {
		return alreadySaved.contains(getKey(t));
	}

	private ChangeMonitorKey getKey(Object t) {
		return new ChangeMonitorKey(t.getClass(), mappingHelper.getKey(t));
	}

	private static class ChangeMonitorKey {
		Class type;
		Object id;

		public ChangeMonitorKey(Class type, Object id) {
			this.type = type;
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ChangeMonitorKey that = (ChangeMonitorKey) o;
			return Objects.equals(type, that.type) && Objects.equals(id, that.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, id);
		}
	}
}
