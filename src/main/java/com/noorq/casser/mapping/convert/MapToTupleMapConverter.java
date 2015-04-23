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
package com.noorq.casser.mapping.convert;

import java.util.Map;
import java.util.function.Function;

import com.datastax.driver.core.TupleType;
import com.noorq.casser.core.SessionRepository;
import com.noorq.casser.support.Transformers;

public final class MapToTupleMapConverter implements Function<Object, Object> {

	final TupleValueWriter keyWriter;
	final TupleValueWriter valueWriter;
	
	public MapToTupleMapConverter(Class<?> keyClass, TupleType keyType, Class<?> valueClass, TupleType valueType, SessionRepository repository) {
		this.keyWriter = new TupleValueWriter(keyClass, keyType, repository);
		this.valueWriter = new TupleValueWriter(valueClass, valueType, repository);
	}
	
	@Override
	public Object apply(Object t) {
		return Transformers.transformMap((Map<Object, Object>) t, keyWriter, valueWriter);
	}

}