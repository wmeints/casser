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
package com.noorq.casser.core.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.UDTValue;
import com.noorq.casser.core.Casser;
import com.noorq.casser.mapping.CasserEntity;
import com.noorq.casser.mapping.CasserMappingEntity;
import com.noorq.casser.mapping.CasserProperty;
import com.noorq.casser.mapping.type.AbstractDataType;
import com.noorq.casser.mapping.type.DTDataType;
import com.noorq.casser.mapping.type.UDTDataType;
import com.noorq.casser.support.CasserException;
import com.noorq.casser.support.DslPropertyException;

public class DslInvocationHandler<E> implements InvocationHandler {

	private final CasserEntity entity;
	private final Optional<CasserPropertyNode> parent;
	
	private final Map<Method, CasserProperty> map = new HashMap<Method, CasserProperty>();
	
	private final Map<Method, Object> udtMap = new HashMap<Method, Object>();
	private final Map<Method, Object> tupleMap = new HashMap<Method, Object>();

	public DslInvocationHandler(Class<E> iface, ClassLoader classLoader, Optional<CasserPropertyNode> parent) {
		
		this.entity = new CasserMappingEntity(iface);
		this.parent = parent;
		
		for (CasserProperty prop : entity.getOrderedProperties()) {
			
			map.put(prop.getGetterMethod(), prop);
			
			AbstractDataType type = prop.getDataType();
			Class<?> javaType = prop.getJavaType();
			
			if (type instanceof UDTDataType && !UDTValue.class.isAssignableFrom(javaType)) {

				Object childDsl = Casser.dsl(javaType, classLoader,
						Optional.of(new CasserPropertyNode(prop, parent)));
				
				udtMap.put(prop.getGetterMethod(), childDsl);
			}
			
			if (type instanceof DTDataType) {
				DTDataType dataType = (DTDataType) type;
				
				if (dataType.getDataType() instanceof TupleType && !TupleValue.class.isAssignableFrom(javaType)) {
					
					Object childDsl = Casser.dsl(javaType, classLoader,
							Optional.of(new CasserPropertyNode(prop, parent)));
					
					tupleMap.put(prop.getGetterMethod(), childDsl);
					
				}
			}
			
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		String methodName = method.getName();
		
		if ("equals".equals(methodName) && method.getParameterCount() == 1) {
			Object otherObj = args[0];
			if (otherObj == null) {
				return false;
			}
			if (Proxy.isProxyClass(otherObj.getClass())) {
				return this == Proxy.getInvocationHandler(otherObj);
			}
			return false;
		}
		
		if (method.getParameterCount() != 0 || method.getReturnType() == void.class) {
			throw new CasserException("invalid getter method " + method);
		}
		
		if ("hashCode".equals(methodName)) {
			return hashCode();
		}
		
		if ("toString".equals(methodName)) {
			return entity.toString();
		}
		
		if (DslExportable.GET_ENTITY_METHOD.equals(methodName)) {
			return entity;
		}
		
		if (DslExportable.GET_PARENT_METHOD.equals(methodName)) {
			return parent.get();
		}
		
		CasserProperty prop = map.get(method);
		
		if (prop != null) {
			
			AbstractDataType type = prop.getDataType();
			
			if (type instanceof UDTDataType) {
				
				Object childDsl = udtMap.get(method);
				
				if (childDsl != null) {
					return childDsl;
				}
				
			}
			
			if (type instanceof DTDataType) {
				DTDataType dataType = (DTDataType) type;
				DataType dt = dataType.getDataType();
				
				switch(dt.getName()) {
				
				case TUPLE:
					
					Object childDsl = tupleMap.get(method);
					
					if (childDsl != null) {
						return childDsl;
					}
					
					break;
					
				case SET:
					return new SetDsl(new CasserPropertyNode(prop, parent));
					
				case LIST:
					return new ListDsl(new CasserPropertyNode(prop, parent));

				case MAP:
					return new MapDsl(new CasserPropertyNode(prop, parent));

				default:
					break;
					
				}
				

			}
			
			throw new DslPropertyException(new CasserPropertyNode(prop, parent));	
		}
		
		throw new CasserException("invalid method call " + method);
	}

}
