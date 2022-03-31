package com.valqueries.automapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GroupStringResultImpl implements GroupStringResult {
	private Map<Grouping, String> result;

	public GroupStringResultImpl(Map<Grouping, String> result) {
		this.result = result;
	}

	@Override
	public int size() {
		return result.size();
	}

	@Override
	public String get(Object... groupValues) {
		List<Object> groupValueList = Arrays.asList(groupValues);

		for (Map.Entry<Grouping, String> entry : result.entrySet()) {
			if (entry.getKey().matches(groupValueList)) {
				return entry.getValue();
			}
		}
		return "";
	}

	@Override
	public List<List<Object>> keys() {
		return new ArrayList<>(result.keySet());
	}


	public static class Grouping extends ArrayList<Object> {
		private final Object value;

		public Grouping(List<Object> grouping, Object value) {
			addAll(grouping);
			this.value = value;
		}

		public boolean matches(List<Object> groupValueList) {
			if (groupValueList.size() != size()) {
				return false;
			}
			for(int i=0;i < size(); i++) {
				if(!groupValueList.get(i).equals(get(i))) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = super.equals(o);
			return eq;
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			return result;
		}

		public Object getValue() {
			return value;
		}
	}
}
