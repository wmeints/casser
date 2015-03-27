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
package casser.mapping.convert;

import java.util.function.Function;

import casser.mapping.CasserMappingRepository;

import com.datastax.driver.core.UDTValue;

public final class UDTValueToEntityConverter implements Function<UDTValue, Object>, ConverterRepositoryAware {

	private final Class<?> iface;
	private CasserMappingRepository repository;
	
	public UDTValueToEntityConverter(Class<?> iface) {
		this.iface = iface;
	}

	@Override
	public void setRepository(CasserMappingRepository repository) {
		this.repository = repository;
	}

	@Override
	public Object apply(UDTValue source) {
		
		System.out.println("UDTValue to interface " + source);
		
		return null;
	}

}