/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.tx;

import java.util.List;

import com.orientechnologies.orient.core.record.ORecordInternal;

public interface OTransaction {
	public enum TXTYPE {
		NOTX, OPTIMISTIC, PESSIMISTIC
	}

	public enum TXSTATUS {
		INVALID, BEGUN, COMMITTING, ROLLBACKING
	}

	public ORecordInternal<?> load(int iClusterId, long iPosition, ORecordInternal<?> iRecord, String iFetchPlan);

	public void save(ORecordInternal<?> iContent, String iClusterName);

	public void delete(ORecordInternal<?> iRecord);

	public void begin();

	public void commit();

	public void rollback();

	public TXSTATUS getStatus();

	public int getId();

	public Iterable<? extends OTransactionEntry> getEntries();

	public List<OTransactionEntry> getEntriesByClass(String iClassName);

	public List<OTransactionEntry> getEntriesByClusterIds(int[] iIds);

	public void clearEntries();

	public int size();
}