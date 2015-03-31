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
import java.util.stream.Collectors;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import com.noorq.casser.mapping.CasserMappingEntity;
import com.noorq.casser.support.CasserException;

public final class TableOperations {

	private final AbstractSessionOperations sessionOps;
	private final boolean dropRemovedColumns;
	
	public TableOperations(AbstractSessionOperations sessionOps, boolean dropRemovedColumns) {
		this.sessionOps = sessionOps;
		this.dropRemovedColumns = dropRemovedColumns;
	}
	
	public void createTable(CasserMappingEntity entity) {
		
		sessionOps.execute(SchemaUtil.createTable(entity));
		
		List<SchemaStatement> list = entity.getMappingProperties().stream()
		.filter(p -> p.getIndexName().isPresent())
		.map(p -> SchemaUtil.createIndex(p))
		.collect(Collectors.toList());
		
		executeBatch(list);
		
	}
	
	public void validateTable(TableMetadata tmd, CasserMappingEntity entity) {
		
		if (tmd == null) {
			throw new CasserException("table not exists " + entity.getName() + "for entity " + entity.getMappingInterface());
		}
		
		List<SchemaStatement> list = SchemaUtil.alterTable(tmd, entity, dropRemovedColumns);
		
		addAlterIndexes(tmd, entity, list);
		
		if (!list.isEmpty()) {
			throw new CasserException("schema changed for entity " + entity.getMappingInterface() + ", apply this command: " + list);
		}
	}
	
	public void updateTable(TableMetadata tmd, CasserMappingEntity entity) {
		
		if (tmd == null) {
			createTable(entity);
			return;
		}
		
		List<SchemaStatement> list = SchemaUtil.alterTable(tmd, entity, dropRemovedColumns);
		addAlterIndexes(tmd, entity, list);

		executeBatch(list);
	}

	private void addAlterIndexes(TableMetadata tmd, CasserMappingEntity entity, List<SchemaStatement> list) {
		

		
	}
	
	private void executeBatch(List<SchemaStatement> list) {
		if (!list.isEmpty()) {
			Batch b = QueryBuilder.batch(list.toArray(new RegularStatement[list.size()]));
			sessionOps.execute(b);
		}
	}
	
}