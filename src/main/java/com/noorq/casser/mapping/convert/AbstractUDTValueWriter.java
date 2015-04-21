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

import java.nio.ByteBuffer;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.noorq.casser.core.SessionRepository;
import com.noorq.casser.mapping.CasserProperty;
import com.noorq.casser.mapping.value.UDTColumnValuePreparer;

public abstract class AbstractUDTValueWriter extends AbstractEntityValueWriter<UDTValue> {

	protected final UserType userType;
	protected final UDTColumnValuePreparer valuePreparer;
	
	public AbstractUDTValueWriter(Class<?> iface, UserType userType, SessionRepository repository) {
		super(iface);
		
		this.userType = userType;
		this.valuePreparer = new UDTColumnValuePreparer(userType, repository);
	}
	
	@Override
	void writeColumn(UDTValue udtValue, Object value,
			CasserProperty prop) {
		
		ByteBuffer bytes = (ByteBuffer) valuePreparer.prepareColumnValue(value, prop);
		
		if (bytes != null) {
			udtValue.setBytesUnsafe(prop.getColumnName().getName(), bytes);
		}
	}
	
}