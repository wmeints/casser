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
package com.noorq.casser.mapping.javatype;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import com.datastax.driver.core.DataType;
import com.noorq.casser.mapping.ColumnType;
import com.noorq.casser.mapping.annotation.Types;
import com.noorq.casser.mapping.type.AbstractDataType;
import com.noorq.casser.mapping.type.DTDataType;

public final class LongJavaType extends AbstractJavaType {

	@Override
	public Class<?> getJavaClass() {
		return Long.class;
	}
	
	@Override
	public Optional<Class<?>> getPrimitiveJavaClass() {
		return Optional.of(long.class);
	}

	@Override
	public AbstractDataType resolveDataType(Method getter, Type genericJavaType, ColumnType columnType) {

		if (null != getter.getDeclaredAnnotation(Types.Counter.class)) {
			 return new DTDataType(columnType, DataType.counter());
		}

		if (null != getter.getDeclaredAnnotation(Types.Bigint.class)) {
			 return new DTDataType(columnType, DataType.bigint());
		}

		return new DTDataType(columnType, DataType.bigint());
	}
	


}
