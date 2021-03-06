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
package com.orientechnologies.orient.core.db.record;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.ORecordFactory;
import com.orientechnologies.orient.core.record.ORecordInternal;

/**
 * Lazy implementation of LinkedHashMap. It's bound to a source ORecord object to keep track of changes. This avoid to call the
 * makeDirty() by hand when the map is changed.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 */
@SuppressWarnings({ "serial" })
public class OLazyRecordMap extends LinkedHashMap<String, Object> {
	final private ORecord<?>	sourceRecord;
	final private byte				recordType;
	private boolean						converted	= false;

	public OLazyRecordMap(final ORecord<?> iSourceRecord, final byte iRecordType) {
		this.sourceRecord = iSourceRecord;
		this.recordType = iRecordType;
	}

	@Override
	public boolean containsValue(final Object o) {
		convertAll();
		return super.containsValue(o);
	}

	@Override
	public Object get(final Object iKey) {
		if (iKey == null)
			return null;

		final String key = iKey.toString();

		convert(key);
		return super.get(key);
	}

	@Override
	public Object put(final String iKey, final Object iValue) {
		if (converted && iValue instanceof ORID)
			converted = false;
		setDirty();
		return super.put(iKey, iValue);
	}

	@Override
	public Object remove(final Object iKey) {
		setDirty();
		return super.remove(iKey);
	}

	@Override
	public Collection<Object> values() {
		convertAll();
		return super.values();
	}

	@Override
	public void clear() {
		setDirty();
		super.clear();
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		setDirty();
		super.putAll(m);
	}

	public void setDirty() {
		if (sourceRecord != null)
			sourceRecord.setDirty();
	}

	private void convertAll() {
		if (converted)
			return;

		for (String key : super.keySet())
			convert(key);
		converted = true;
	}

	/**
	 * Convert the item requested.
	 *
	 * @param iIndex
	 *          Position of the item to convert
	 */
	private void convert(final String iKey) {
		final Object o = super.get(iKey);

		if (o != null && o instanceof ORecordId) {
			final ORecordInternal<?> record = ORecordFactory.newInstance(recordType);
			final ORecordId rid = (ORecordId) o;

			record.setDatabase(sourceRecord.getDatabase());
			record.setIdentity(rid);
			record.load();

			super.put(iKey, record);
		}
	}
}