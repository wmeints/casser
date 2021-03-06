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

import java.util.function.Function;
import java.util.stream.Stream;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.BuiltStatement;


public final class SelectTransformingOperation<R, E> extends AbstractFilterStreamOperation<R, SelectTransformingOperation<R, E>> {

	private final SelectOperation<E> src;
	private final Function<E, R> fn;
	
	public SelectTransformingOperation(SelectOperation<E> src, Function<E, R> fn) {
		super(src.sessionOps);
		
		this.src = src;
		this.fn = fn;
		this.filters = src.filters;
		this.ifFilters = src.ifFilters;
	}
	
	@Override
	public BuiltStatement buildStatement() {
		return src.buildStatement();
	}

	@Override
	public Stream<R> transform(ResultSet resultSet) {
		return src.transform(resultSet).map(fn);
	}
	
	
}
