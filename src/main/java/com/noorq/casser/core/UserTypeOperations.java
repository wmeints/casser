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
package com.noorq.casser.core;

import java.util.List;

import com.datastax.driver.core.UserType;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import com.noorq.casser.mapping.CasserEntity;
import com.noorq.casser.support.CasserException;

public final class UserTypeOperations {
	
	private final AbstractSessionOperations sessionOps;
	private final boolean dropUnusedColumns;
	
	public UserTypeOperations(AbstractSessionOperations sessionOps, boolean dropUnusedColumns) {
		this.sessionOps = sessionOps;
		this.dropUnusedColumns = dropUnusedColumns;
	}
	
	public void createUserType(CasserEntity entity) {
		
		sessionOps.execute(SchemaUtil.createUserType(entity), true);
		
	}

	public void validateUserType(UserType userType, CasserEntity entity) {
		
		if (userType == null) {
			throw new CasserException("userType not exists " + entity.getName() + "for entity " + entity.getMappingInterface());
		}
		
		List<SchemaStatement> list = SchemaUtil.alterUserType(userType, entity, dropUnusedColumns);
		
		if (!list.isEmpty()) {
			throw new CasserException("schema changed for entity " + entity.getMappingInterface() + ", apply this command: " + list);
		}
		
	}

	
	public void updateUserType(UserType userType, CasserEntity entity) {
		
		if (userType == null) {
			createUserType(entity);
			return;
		}
		
		executeBatch(SchemaUtil.alterUserType(userType, entity, dropUnusedColumns));
		
	}

	private void executeBatch(List<SchemaStatement> list) {
		
		list.forEach(s -> {
			sessionOps.execute(s, true);
		});
		
	}

}
