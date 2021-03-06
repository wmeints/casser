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

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;
import com.google.common.util.concurrent.MoreExecutors;
import com.noorq.casser.mapping.CasserEntity;
import com.noorq.casser.mapping.CasserEntityType;
import com.noorq.casser.mapping.value.ColumnValuePreparer;
import com.noorq.casser.mapping.value.ColumnValueProvider;
import com.noorq.casser.support.CasserException;
import com.noorq.casser.support.PackageUtil;


public final class SessionInitializer extends AbstractSessionOperations {

	private final Session session;
	private String usingKeyspace;
	private boolean showCql = false;
	private PrintStream printStream = System.out;
	private Executor executor = MoreExecutors.sameThreadExecutor();
	
	private SessionRepositoryBuilder sessionRepository = new SessionRepositoryBuilder();
	
	private boolean dropUnusedColumns = false;
	private boolean dropUnusedIndexes = false;
	
	private KeyspaceMetadata keyspaceMetadata;
	
	private final List<Object> initList = new ArrayList<Object>();
	private AutoDdl autoDdl = AutoDdl.UPDATE;
	
	SessionInitializer(Session session) {
		this.session = Objects.requireNonNull(session, "empty session");
		this.usingKeyspace = session.getLoggedKeyspace(); // can be null
	}
	
	@Override
	public Session currentSession() {
		return session;
	}

	@Override
	public String usingKeyspace() {
		return usingKeyspace;
	}
	
	@Override
	public Executor getExecutor() {
		return executor;
	}

	@Override
	public SessionRepository getSessionRepository() {
		throw new CasserException("not expected to call");
	}

	@Override
	public ColumnValueProvider getValueProvider() {
		throw new CasserException("not expected to call");
	}
	
	@Override
	public ColumnValuePreparer getValuePreparer() {
		throw new CasserException("not expected to call");
	}

	public SessionInitializer showCql() {
		this.showCql = true;
		return this;
	}
	
	public SessionInitializer showCql(boolean enabled) {
		this.showCql = enabled;
		return this;
	}

	@Override
	public PrintStream getPrintStream() {
		return printStream;
	}

	public SessionInitializer printTo(PrintStream out) {
		this.printStream = out;
		return this;
	}
	
	public SessionInitializer withExecutor(Executor executor) {
		Objects.requireNonNull(executor, "empty executor");
		this.executor = executor;
		return this;
	}

	public SessionInitializer withCachingExecutor() {
		this.executor = Executors.newCachedThreadPool();
		return this;
	}

	public SessionInitializer dropUnusedColumns(boolean enabled) {
		this.dropUnusedColumns = enabled;
		return this;
	}

	public SessionInitializer dropUnusedIndexes(boolean enabled) {
		this.dropUnusedIndexes = enabled;
		return this;
	}

	@Override
	public boolean isShowCql() {
		return showCql;
	}
	
	public SessionInitializer addPackage(String packageName) {
		try {
			PackageUtil.getClasses(packageName)
				.stream()
				.filter(c -> c.isInterface() && !c.isAnnotation())
				.forEach(initList::add);
		} catch (ClassNotFoundException e) {
			throw new CasserException("fail to add package " + packageName, e);
		}
		return this;
	}
	
	public SessionInitializer add(Object... dsls) {
		Objects.requireNonNull(dsls, "dsls is empty");
		int len = dsls.length;
		for (int i = 0; i != len; ++i) {
			Object obj = Objects.requireNonNull(dsls[i], "element " + i + " is empty");
			initList.add(obj);
		}
		return this;
	}
	
	public SessionInitializer autoValidate() {
		this.autoDdl = AutoDdl.VALIDATE;
		return this;
	}

	public SessionInitializer autoUpdate() {
		this.autoDdl = AutoDdl.UPDATE;
		return this;
	}

	public SessionInitializer autoCreate() {
		this.autoDdl = AutoDdl.CREATE;
		return this;
	}

	public SessionInitializer autoCreateDrop() {
		this.autoDdl = AutoDdl.CREATE_DROP;
		return this;
	}

	public SessionInitializer auto(AutoDdl autoDdl) {
		this.autoDdl = autoDdl;
		return this;
	}
	
	public SessionInitializer use(String keyspace) {
		session.execute(SchemaUtil.use(keyspace, false));
		this.usingKeyspace = keyspace;
		return this;
	}
	
	public SessionInitializer use(String keyspace, boolean forceQuote) {
		session.execute(SchemaUtil.use(keyspace, forceQuote));
		this.usingKeyspace = keyspace;
		return this;
	}
	
	public void singleton() {
		Casser.setSession(get());
	}
	
	public synchronized CasserSession get() {
		initialize();
		return new CasserSession(session, 
				usingKeyspace,
				showCql, 
				printStream,
				sessionRepository,
				executor,
				autoDdl == AutoDdl.CREATE_DROP);
	}

	private void initialize() {
		
		Objects.requireNonNull(usingKeyspace, "please define keyspace by 'use' operator");

		initList.forEach(dsl -> sessionRepository.add(dsl));

		TableOperations tableOps = new TableOperations(this, dropUnusedColumns, dropUnusedIndexes);
		UserTypeOperations userTypeOps = new UserTypeOperations(this, dropUnusedColumns);
		
		
		switch(autoDdl) {
		
		case CREATE:
		case CREATE_DROP:

			eachUserTypeInOrder(userTypeOps, e -> userTypeOps.createUserType(e));
			
			sessionRepository.entities().stream().filter(e -> e.getType() == CasserEntityType.TABLE)
				.forEach(e -> tableOps.createTable(e));
			
			break;
			
		case VALIDATE:
			
			eachUserTypeInOrder(userTypeOps, e -> userTypeOps.validateUserType(getUserType(e), e));
			
			sessionRepository.entities().stream().filter(e -> e.getType() == CasserEntityType.TABLE)
				.forEach(e -> tableOps.validateTable(getTableMetadata(e), e));
			break;
			
		case UPDATE:
			
			eachUserTypeInOrder(userTypeOps, e -> userTypeOps.updateUserType(getUserType(e), e));

			sessionRepository.entities().stream().filter(e -> e.getType() == CasserEntityType.TABLE)
				.forEach(e -> tableOps.updateTable(getTableMetadata(e), e));
			break;
		
		}
		
		KeyspaceMetadata km = getKeyspaceMetadata();
		
		for (UserType userType : km.getUserTypes()) {
			sessionRepository.addUserType(userType.getTypeName(), userType);
		}
		
	}
	
	private void eachUserTypeInOrder(UserTypeOperations userTypeOps, Consumer<? super CasserEntity> action) {
		
		Set<CasserEntity> processedSet = new HashSet<CasserEntity>();
		Set<CasserEntity> stack = new HashSet<CasserEntity>();
		
		sessionRepository.entities().stream()
		.filter(e -> e.getType() == CasserEntityType.UDT)
		.forEach(e -> {
		
			stack.clear();
			eachUserTypeInRecursion(e, processedSet, stack, userTypeOps, action);
			
		});
		

	}
	
	private void eachUserTypeInRecursion(CasserEntity e, Set<CasserEntity> processedSet, Set<CasserEntity> stack, UserTypeOperations userTypeOps, Consumer<? super CasserEntity> action) {
		
		stack.add(e);
		
		Collection<CasserEntity> createBefore = sessionRepository.getUserTypeUses(e);
		
		for (CasserEntity be : createBefore) {
			if (!processedSet.contains(be) && !stack.contains(be)) {
				eachUserTypeInRecursion(be, processedSet, stack, userTypeOps, action);
				processedSet.add(be);
			}
		}
		
		if (!processedSet.contains(e)) {
			action.accept(e);
			processedSet.add(e);
		}
		
	}
	
	private KeyspaceMetadata getKeyspaceMetadata() {
		if (keyspaceMetadata == null) {
			keyspaceMetadata = session.getCluster().getMetadata().getKeyspace(usingKeyspace.toLowerCase());
		}
		return keyspaceMetadata;
	}
	
	private TableMetadata getTableMetadata(CasserEntity entity) {
		return getKeyspaceMetadata().getTable(entity.getName().getName());
		
	}

	private UserType getUserType(CasserEntity entity) {
		return getKeyspaceMetadata().getUserType(entity.getName().getName());
	}
}
