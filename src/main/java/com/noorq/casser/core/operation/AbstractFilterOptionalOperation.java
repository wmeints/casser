/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.noorq.casser.core.operation;

import java.util.LinkedList;
import java.util.List;

import com.noorq.casser.core.AbstractSessionOperations;
import com.noorq.casser.core.Filter;
import com.noorq.casser.core.Getter;
import com.noorq.casser.core.Operator;
import com.noorq.casser.core.Postulate;

public abstract class AbstractFilterOptionalOperation<E, O extends AbstractFilterOptionalOperation<E, O>> extends AbstractOptionalOperation<E, O> {

	protected List<Filter<?>> filters = null;
	protected List<Filter<?>> ifFilters = null;
	
	public AbstractFilterOptionalOperation(AbstractSessionOperations sessionOperations) {
		super(sessionOperations);
	}
	
	public <V> O where(Getter<V> getter, Postulate<V> postulate) {
		
		addFilter(Filter.create(getter, postulate));
		
		return (O) this;
	}

	public <V> O where(Getter<V> getter, Operator operator, V val) {
		
		addFilter(Filter.create(getter, operator, val));
		
		return (O) this;
	}
	
	public <V> O where(Filter<V> filter) {

		addFilter(filter);

		return (O) this;
	}

	public <V> O and(Getter<V> getter, Postulate<V> postulate) {
		
		addFilter(Filter.create(getter, postulate));
		
		return (O) this;
	}
	
	public <V> O and(Getter<V> getter, Operator operator, V val) {
		
		addFilter(Filter.create(getter, operator, val));
		
		return (O) this;
	}

	public <V> O and(Filter<V> filter) {
		
		addFilter(filter);
		
		return (O) this;
	}
	
	public <V> O onlyIf(Getter<V> getter, Postulate<V> postulate) {
		
		addIfFilter(Filter.create(getter, postulate));
		
		return (O) this;
	}

	public <V> O onlyIf(Getter<V> getter, Operator operator, V val) {
		
		addIfFilter(Filter.create(getter, operator, val));
		
		return (O) this;
	}
	
	public <V> O onlyIf(Filter<V> filter) {

		addIfFilter(filter);

		return (O) this;
	}
	
	private void addFilter(Filter<?> filter) {
		if (filters == null) {
			filters = new LinkedList<Filter<?>>();
		}
		filters.add(filter);
	}

	private void addIfFilter(Filter<?> filter) {
		if (ifFilters == null) {
			ifFilters = new LinkedList<Filter<?>>();
		}
		ifFilters.add(filter);
	}

}
