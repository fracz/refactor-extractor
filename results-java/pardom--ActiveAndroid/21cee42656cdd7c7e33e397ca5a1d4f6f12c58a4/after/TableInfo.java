package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.util.Log;

public class TableInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Class<? extends Model> mType;
	private String mTableName;

	private Map<Field, String> mColumnNames = new HashMap<Field, String>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public TableInfo(Class<? extends Model> type) {
		mType = type;

		final Table tableAnnotation = type.getAnnotation(Table.class);
		if (tableAnnotation != null) {
			mTableName = tableAnnotation.name();
		}
		else {
			mTableName = type.getSimpleName();
		}

		Field idField = getIdField(type);
		mColumnNames.put(idField, idField.getName());

		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				final Column columnAnnotation = field.getAnnotation(Column.class);
				mColumnNames.put(field, columnAnnotation.name());
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public Class<? extends Model> getType() {
		return mType;
	}

	public String getTableName() {
		return mTableName;
	}

	public List<Field> getFields() {
		return new ArrayList(mColumnNames.keySet());
	}

	public String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Field getIdField(Class<?> type) {
		if (type.equals(Model.class)) {
			try {
				return type.getDeclaredField("mId");
			}
			catch (NoSuchFieldException e) {
				Log.e("Impossible!", e);
			}
		}
		else if (type.getSuperclass() != null) {
			return getIdField(type.getSuperclass());
		}

		return null;
	}
}