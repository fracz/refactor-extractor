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
package com.orientechnologies.orient.core.db.tool;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.common.parser.OStringForwardReader;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.OJSONReader;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;
import com.orientechnologies.orient.core.serialization.serializer.record.string.ORecordSerializerJSON;
import com.orientechnologies.orient.core.storage.OStorage;

/**
 * Import data from a file into a database.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 */
public class ODatabaseImport extends ODatabaseImpExpAbstract {
	private Map<OProperty, String>	linkedClasses		= new HashMap<OProperty, String>();
	private Map<OClass, String>			superClasses		= new HashMap<OClass, String>();
	private OJSONReader							jsonReader;
	private OStringForwardReader		reader;
	private ORecordInternal<?>			record;
	private Set<String>							recordToDelete	= new HashSet<String>();
	private Map<OProperty, String>	propertyIndexes	= new HashMap<OProperty, String>();

	public ODatabaseImport(final ODatabaseDocument database, final String iFileName, final OCommandOutputListener iListener)
			throws IOException {
		super(database, iFileName, iListener);
		jsonReader = new OJSONReader(new FileReader(fileName));
		database.declareIntent(new OIntentMassiveInsert());
	}

	public ODatabaseImport(final ODatabaseDocument database, final InputStream iStream, final OCommandOutputListener iListener)
			throws IOException {
		super(database, "streaming", iListener);
		jsonReader = new OJSONReader(new InputStreamReader(iStream));
		database.declareIntent(new OIntentMassiveInsert());
	}

	public ODatabaseImport importDatabase() {
		try {
			listener.onMessage("\nStarted import of database '" + database.getName() + "' from " + fileName + "...");

			long time = System.currentTimeMillis();

			jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

			String tag;
			while (jsonReader.hasNext() && jsonReader.lastChar() != '}') {
				tag = jsonReader.readString(OJSONReader.FIELD_ASSIGNMENT);

				if (tag.equals("info"))
					importInfo();
				else if (tag.equals("clusters"))
					importClusters();
				else if (tag.equals("schema"))
					importSchema();
				else if (tag.equals("records"))
					importRecords();
				else if (tag.equals("indexes"))
					importManualIndexes();
				else if (tag.equals("dictionary"))
					importDictionary();
			}

			deleteHoleRecords();
			rebuildAutomaticIndexes();

			listener.onMessage("\n\nDatabase import completed in " + ((System.currentTimeMillis() - time)) + " ms");

		} catch (Exception e) {
			System.err.println("Error on database import happened just before line " + jsonReader.getLineNumber() + ", column "
					+ jsonReader.getColumnNumber());
			e.printStackTrace();
			throw new ODatabaseExportException("Error on importing database '" + database.getName() + "' from file: " + fileName, e);
		} finally {
			close();
		}

		return this;
	}

	private void rebuildAutomaticIndexes() {
		listener.onMessage("\nRebuilding " + propertyIndexes.size() + " indexes...");

		for (Entry<OProperty, String> e : propertyIndexes.entrySet()) {
			e.getKey().setIndex(database.getMetadata().getIndexManager().getIndex(e.getValue()));
			e.getKey().getIndex().getUnderlying().setCallback(e.getKey().getIndex());

			// listener.onMessage("\n- Index '" + e.getKey().getIndex().getUnderlying().getName() + "'...");
			//
			// e.getKey().getIndex().getUnderlying().rebuild(new OProgressListener() {
			// public boolean onProgress(Object iTask, long iCounter, float iPercent) {
			// if (iPercent % 10 == 0)
			// listener.onMessage(".");
			// return false;
			// }
			//
			// public void onCompletition(Object iTask, boolean iSucceed) {
			// }
			//
			// public void onBegin(Object iTask, long iTotal) {
			// }
			// });
			//
			// listener.onMessage("OK (" + e.getKey().getIndex().getUnderlying().getSize() + " records)");
		}
	}

	/**
	 * Delete all the temporary records created to fill the holes and to mantain the same record ID
	 */
	private void deleteHoleRecords() {
		listener.onMessage("\nDelete temporary records...");

		final ORecordId rid = new ORecordId();
		final ODocument doc = new ODocument(database, rid);
		for (String recId : recordToDelete) {
			doc.reset();
			rid.fromString(recId);
			doc.delete();
		}
		listener.onMessage("OK (" + recordToDelete.size() + " records)");
	}

	private void importInfo() throws IOException, ParseException {
		listener.onMessage("\nImporting database info...");

		jsonReader.readNext(OJSONReader.COMMA_SEPARATOR);
		jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT);
		@SuppressWarnings("unused")
		int defClusterId = jsonReader.readNumber(OJSONReader.ANY_NUMBER, true);
		jsonReader.readNext(OJSONReader.END_OBJECT);
		jsonReader.readNext(OJSONReader.COMMA_SEPARATOR);

		listener.onMessage("OK");
	}

	@SuppressWarnings("unused")
	private long importDictionary() throws IOException, ParseException {
		listener.onMessage("\nImporting database dictionary...");

		String dictionaryKey;
		String dictionaryValue;

		final ODocument doc = new ODocument(database);
		final ORecordId rid = new ORecordId();

		long tot = 0;

		jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

		do {
			dictionaryKey = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"key\"")
					.readString(OJSONReader.COMMA_SEPARATOR);
			dictionaryValue = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"value\"")
					.readString(OJSONReader.NEXT_IN_OBJECT);

			if (dictionaryValue.length() >= 4)
				rid.fromString(dictionaryValue.substring(1));

			// AVOID TO CHANGE THE DICTIONARY BECAUSE IT'S IMPORTED BY UNDERLYING RECORDS
			// ((ODictionary<ODocument>) database.getDictionary()).put(dictionaryKey, doc);
			tot++;
		} while (jsonReader.lastChar() == ',');

		listener.onMessage("OK (" + tot + " entries)");

		jsonReader.readNext(OJSONReader.END_OBJECT);

		return tot;
	}

	@SuppressWarnings("unused")
	private void importManualIndexes() throws IOException, ParseException {
		listener.onMessage("\nImporting manual indexes...");

		String key;
		String value;

		final ODocument doc = new ODocument(database);

		// FORCE RELOADING
		database.getMetadata().getIndexManager().load();

		jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

		do {
			final String indexName = jsonReader.readString(OJSONReader.FIELD_ASSIGNMENT);

			listener.onMessage("\n- Index '" + indexName + "'...");

			final OIndex index = database.getMetadata().getIndexManager().getIndex(indexName);

			long tot = 0;

			jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

			String n;
			do {
				jsonReader.readNext(new char[] { ':', '}' });

				if (jsonReader.lastChar() != '}') {
					key = jsonReader.checkContent("\"key\"").readString(OJSONReader.COMMA_SEPARATOR);
					value = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"value\"")
							.readString(OJSONReader.NEXT_IN_OBJECT);

					if (index != null)
						if (value.length() >= 4) {
							if (value.charAt(0) == '[')
								// REMOVE []
								value = value.substring(1, value.length() - 1);

							List<String> rids = OStringSerializerHelper.split(value, ',', new char[] { '#', '"' });

							for (String rid : rids) {
								doc.setIdentity(new ORecordId(rid));
								index.put(key, doc);
							}
						}

					tot++;
				}
			} while (jsonReader.lastChar() == ',');

			if (index != null)
				listener.onMessage("OK (" + tot + " entries)");
			else
				listener.onMessage("KO, the index wasn't found in configuration");

			jsonReader.readNext(OJSONReader.NEXT_IN_OBJECT);

		} while (jsonReader.lastChar() == ',');

	}

	private void importSchema() throws IOException, ParseException {
		listener.onMessage("\nImporting database schema...");

		jsonReader.readNext(OJSONReader.BEGIN_OBJECT);
		@SuppressWarnings("unused")
		int schemaVersion = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"version\"")
				.readNumber(OJSONReader.ANY_NUMBER, true);
		jsonReader.readNext(OJSONReader.COMMA_SEPARATOR).readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"classes\"")
				.readNext(OJSONReader.BEGIN_COLLECTION);

		long classImported = 0;
		String className;
		int classId;
		int classDefClusterId;
		String classClusterIds;
		String classSuper = null;

		OClass cls;

		try {
			do {
				jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

				className = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"name\"")
						.readString(OJSONReader.COMMA_SEPARATOR);

				classId = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"id\"").readInteger(OJSONReader.COMMA_SEPARATOR);

				classDefClusterId = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"default-cluster-id\"")
						.readInteger(OJSONReader.COMMA_SEPARATOR);

				classClusterIds = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"cluster-ids\"")
						.readString(OJSONReader.NEXT_IN_OBJECT).trim();

				cls = database.getMetadata().getSchema().getClass(className);

				if (cls != null) {
					if (cls.getDefaultClusterId() != classDefClusterId)
						cls.setDefaultClusterId(classDefClusterId);
				} else
					cls = database.getMetadata().getSchema().createClass(className, classDefClusterId);

				if (classId != cls.getId())
					throw new OSchemaException("Imported class '" + className + "' has id=" + cls.getId() + " different from the original: "
							+ classId);

				if (classClusterIds != null) {
					// REMOVE BRACES
					classClusterIds = classClusterIds.substring(1, classClusterIds.length() - 1);

					// ASSIGN OTHER CLUSTER IDS
					for (int i : OStringSerializerHelper.splitIntArray(classClusterIds)) {
						cls.addClusterIds(i);
					}
				}

				String value;
				while (jsonReader.lastChar() == ',') {
					jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT);
					value = jsonReader.getValue();

					if (value.equals("\"super-class\"")) {
						classSuper = jsonReader.readString(OJSONReader.NEXT_IN_OBJECT);
						superClasses.put(cls, classSuper);
					} else if (value.equals("\"properties\"")) {
						// GET PROPERTIES
						jsonReader.readNext(OJSONReader.BEGIN_COLLECTION);

						while (jsonReader.lastChar() != ']') {
							importProperty(cls);

							if (jsonReader.lastChar() == '}')
								jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
						}
						jsonReader.readNext(OJSONReader.END_OBJECT);
					}
				}

				classImported++;

				jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
			} while (jsonReader.lastChar() == ',');

			// REBUILD ALL THE INHERITANCE
			for (Map.Entry<OClass, String> entry : superClasses.entrySet()) {
				entry.getKey().setSuperClass(database.getMetadata().getSchema().getClass(entry.getValue()));
			}

			// SET ALL THE LINKED CLASSES
			for (Map.Entry<OProperty, String> entry : linkedClasses.entrySet()) {
				entry.getKey().setLinkedClass(database.getMetadata().getSchema().getClass(entry.getValue()));
			}

			database.getMetadata().getSchema().save();

			listener.onMessage("OK (" + classImported + " classes)");

			jsonReader.readNext(OJSONReader.END_OBJECT);
			jsonReader.readNext(OJSONReader.COMMA_SEPARATOR);
		} catch (Exception e) {
			e.printStackTrace();
			listener.onMessage("ERROR (" + classImported + " entries): " + e);
		}
	}

	private void importProperty(final OClass iClass) throws IOException, ParseException {
		jsonReader.readNext(OJSONReader.NEXT_OBJ_IN_ARRAY);

		if (jsonReader.lastChar() == ']')
			return;

		String propName = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"name\"")
				.readString(OJSONReader.COMMA_SEPARATOR);

		final int id = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"id\"")
				.readInteger(OJSONReader.COMMA_SEPARATOR);

		final OType type = OType.valueOf(jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"type\"")
				.readString(OJSONReader.NEXT_IN_OBJECT));

		String attrib;
		String value;

		String min = null;
		String max = null;
		String linkedClass = null;
		OType linkedType = null;
		String indexName = null;
		String indexType = null;

		while (jsonReader.lastChar() == ',') {
			jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT);

			attrib = jsonReader.getValue();
			value = jsonReader.readString(OJSONReader.NEXT_IN_OBJECT);

			if (attrib.equals("\"min\""))
				min = value;
			else if (attrib.equals("\"max\""))
				max = value;
			else if (attrib.equals("\"linked-class\""))
				linkedClass = value;
			else if (attrib.equals("\"linked-type\""))
				linkedType = OType.valueOf(value);
			else if (attrib.equals("\"index\""))
				indexName = value;
			else if (attrib.equals("\"index-type\""))
				indexType = value;
		}

		OProperty prop = iClass.getProperty(propName);
		if (prop == null) {
			// CREATE IT
			prop = iClass.createProperty(propName, type);
		} else {
			if (prop.getId() != id)
				throw new OSchemaException("Imported property '" + iClass.getName() + "." + propName
						+ "' has an id different from the original: " + id);
		}

		if (min != null)
			prop.setMin(min);
		if (max != null)
			prop.setMax(max);
		if (linkedClass != null)
			linkedClasses.put(prop, linkedClass);
		if (linkedType != null)
			prop.setLinkedType(linkedType);
		if (indexName != null)
			// PUSH INDEX TO CREATE AFTER ALL
			propertyIndexes.put(prop, indexName);
	}

	private long importClusters() throws ParseException, IOException {
		listener.onMessage("\nImporting clusters...");

		long total = 0;

		jsonReader.readNext(OJSONReader.BEGIN_COLLECTION);

		@SuppressWarnings("unused")
		ORecordId rid = null;
		while (jsonReader.lastChar() != ']') {
			jsonReader.readNext(OJSONReader.BEGIN_OBJECT);

			String name = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"name\"")
					.readString(OJSONReader.COMMA_SEPARATOR);

			// CHECK IF THE CLUSTER IS INCLUDED
			if (includeClusters != null) {
				if (!includeClusters.contains(name)) {
					jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
					continue;
				}
			} else if (excludeClusters != null) {
				if (excludeClusters.contains(name)) {
					jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
					continue;
				}
			}

			int id = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"id\"").readInteger(OJSONReader.COMMA_SEPARATOR);
			String type = jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"type\"")
					.readString(OJSONReader.NEXT_IN_OBJECT);

			if (jsonReader.lastChar() == ',') {
				rid = new ORecordId(jsonReader.readNext(OJSONReader.FIELD_ASSIGNMENT).checkContent("\"rid\"")
						.readString(OJSONReader.NEXT_IN_OBJECT));
			} else
				rid = null;

			listener.onMessage("\n- Creating cluster " + name + "...");

			int clusterId = database.getClusterIdByName(name);
			if (clusterId == -1) {
				// CREATE IT
				if (type.equals("PHYSICAL"))
					clusterId = database.addPhysicalCluster(name, name, -1);
				else if (type.equals("LOGICAL")) {
					clusterId = database.addLogicalCluster(name, database.getClusterIdByName(OStorage.CLUSTER_INTERNAL_NAME));
				}
			}

			if (clusterId != id)
				throw new OConfigurationException("Imported cluster '" + name + "' has id=" + clusterId + " different from the original: "
						+ id);

			listener.onMessage("OK");

			total++;

			jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
		}
		jsonReader.readNext(OJSONReader.COMMA_SEPARATOR);

		listener.onMessage("\nDone. Imported " + total + " clusters");

		return total;
	}

	private long importRecords() throws ParseException, IOException {
		long total = 0;

		jsonReader.readNext(OJSONReader.BEGIN_COLLECTION);

		long totalRecords = 0;

		System.out.print("\nImporting records...");

		ORID rid;
		int lastClusterId = 0;
		long clusterRecords = 0;
		while (jsonReader.lastChar() != ']') {
			rid = importRecord();

			if (rid != null) {
				++clusterRecords;

				if (rid.getClusterId() != lastClusterId) {
					// CHANGED CLUSTERID: DUMP STATISTICS
					System.out.print("\n- Imported records into the cluster '" + database.getClusterNameById(lastClusterId) + "': "
							+ clusterRecords + " records");
					clusterRecords = 0;
				}

				lastClusterId = rid.getClusterId();

				++totalRecords;
			} else
				lastClusterId = 0;
		}

		listener.onMessage("\n\nDone. Imported " + totalRecords + " records\n");

		jsonReader.readNext(OJSONReader.COMMA_SEPARATOR);

		return total;
	}

	private ORID importRecord() throws IOException, ParseException {
		final String value = jsonReader.readString(OJSONReader.END_OBJECT, true);

		record = ORecordSerializerJSON.INSTANCE.fromString(database, value, record);

		// CHECK IF THE CLUSTER IS INCLUDED
		if (includeClusters != null) {
			if (!includeClusters.contains(database.getClusterNameById(record.getIdentity().getClusterId()))) {
				jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
				return null;
			}
		} else if (excludeClusters != null) {
			if (excludeClusters.contains(database.getClusterNameById(record.getIdentity().getClusterId()))) {
				jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);
				return null;
			}
		}

		String rid = record.getIdentity().toString();

		long nextAvailablePos = database.getStorage().getClusterDataRange(record.getIdentity().getClusterId())[1] + 1;

		// SAVE THE RECORD
		if (record.getIdentity().getClusterPosition() < nextAvailablePos) {
			// REWRITE PREVIOUS RECORD
			if (record instanceof ODocument)
				record.save();
			else
				((ODatabaseRecord) database.getUnderlying()).save(record);
		} else {
			String clusterName = database.getClusterNameById(record.getIdentity().getClusterId());

			if (record.getIdentity().getClusterPosition() > nextAvailablePos) {
				// CREATE HOLES
				int holes = (int) (record.getIdentity().getClusterPosition() - nextAvailablePos);

				ODocument tempRecord = new ODocument(database);
				for (int i = 0; i < holes; ++i) {
					tempRecord.reset();
					((ODatabaseRecord) database.getUnderlying()).save(tempRecord, clusterName);
					recordToDelete.add(tempRecord.getIdentity().toString());
				}
			}

			// APPEND THE RECORD
			record.setIdentity(-1, -1);
			if (record instanceof ODocument)
				record.save(clusterName);
			else
				((ODatabaseRecord) database.getUnderlying()).save(record, clusterName);
		}

		if (!record.getIdentity().toString().equals(rid))
			throw new OSchemaException("Imported record '" + record.getIdentity() + "' has rid different from the original: " + rid);

		jsonReader.readNext(OJSONReader.NEXT_IN_ARRAY);

		return record.getIdentity();
	}

	public void close() {
		database.declareIntent(null);

		if (reader == null)
			return;

		try {
			reader.close();
			reader = null;
		} catch (IOException e) {
		}
	}
}