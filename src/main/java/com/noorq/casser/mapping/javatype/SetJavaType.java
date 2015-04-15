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
import java.util.Set;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.UDTValue;
import com.noorq.casser.mapping.ColumnType;
import com.noorq.casser.mapping.IdentityName;
import com.noorq.casser.mapping.annotation.type.CSet;
import com.noorq.casser.mapping.annotation.type.UDTSet;
import com.noorq.casser.mapping.type.AbstractDataType;
import com.noorq.casser.mapping.type.DTDataType;
import com.noorq.casser.mapping.type.UDTSetDataType;
import com.noorq.casser.support.Either;

public final class SetJavaType extends AbstractJavaType {

	@Override
	public Class<?> getJavaClass() {
		return Set.class;
	}

	@Override
	public AbstractDataType resolveDataType(Method getter, Type genericJavaType, ColumnType columnType) {

		CSet cset = getter.getDeclaredAnnotation(CSet.class);
		if (cset != null) {
			return new DTDataType(columnType, 
					DataType.set(resolveSimpleType(getter, cset.value())));
		}

		UDTSet udtSet = getter.getDeclaredAnnotation(UDTSet.class);
		if (udtSet != null) {
			return new UDTSetDataType(columnType, 
					resolveUDT(udtSet.value()),
					UDTValue.class);
		}

		Type[] args = getTypeParameters(genericJavaType);
		ensureTypeArguments(getter, args.length, 1);
		
		Either<DataType, IdentityName> parameterType = autodetectParameterType(getter, args[0]);

		if (parameterType.isLeft()) {
			return new DTDataType(columnType, DataType.set(parameterType.getLeft()));
		}
		else {
			return new UDTSetDataType(columnType, 
					parameterType.getRight(),
					(Class<?>) args[0]);
		}
		
	}
	
}