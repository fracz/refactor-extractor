/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.greenrobot.dao.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoLog;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.SqlUtils;

/**
 * Default tests for single-PK entities.
 *
 * @author Markus
 *
 * @param <D>
 *            DAO class
 * @param <T>
 *            Entity type of the DAO
 * @param <K>
 *            Key type of the DAO
 */
public abstract class AbstractDaoTestSinglePk<D extends AbstractDao<T, K>, T, K> extends AbstractDaoTest<D, T, K> {

    protected Set<K> usedPks;
    private Property pkColumn;

    public AbstractDaoTestSinglePk(Class<D> daoClass) {
        super(daoClass);
        usedPks = new HashSet<K>();
    }

    @Override
    protected void setUp() {
        super.setUp();
        Property[] columns = daoAccess.getProperties();
        for (Property column : columns) {
            if (column.primaryKey) {
                if (pkColumn != null) {
                    throw new RuntimeException("Test does not work with multiple PK columns");
                }
                pkColumn = column;
            }
        }
        if (pkColumn == null) {
            throw new RuntimeException("Test does not work without a PK column");
        }

    }

    public void testInsertAndLoad() {
        K pk = nextPk();
        T entity = createEntity(pk);
        dao.insert(entity);
        T entity2 = dao.load(pk);
        assertNotNull(entity2);
        // assertNotSame(entity, entity2); // Unless we'll cache stuff one day --> we do now
        assertEquals(daoAccess.getKey(entity), daoAccess.getKey(entity2));
    }

    public void testInsertInTx() {
        dao.deleteAll();
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < 20; i++) {
            list.add(createEntityWithRandomPk());
        }
        dao.insertInTx(list);
        assertEquals(list.size(), dao.count());
    }

    public void testAssignPk() {
        if (daoAccess.isEntityUpdateable()) {
            T entity1 = createEntity(null);
            if (entity1 != null) {
                T entity2 = createEntity(null);

                dao.insert(entity1);
                dao.insert(entity2);

                K pk1 = daoAccess.getKey(entity1);
                K pk2 = daoAccess.getKey(entity2);

                assertFalse(pk1.equals(pk2));

                assertNotNull(dao.load(pk1));
                assertNotNull(dao.load(pk2));
            } else {
                DaoLog.d("Skipping testAssignPk for " + daoClass + " (createEntity returned null for null key)");
            }
        } else {
            DaoLog.d("Skipping testAssignPk for not updateable " + daoClass);
        }
    }

    public void testCount() {
        dao.deleteAll();
        assertEquals(0, dao.count());
        dao.insert(createEntityWithRandomPk());
        assertEquals(1, dao.count());
        dao.insert(createEntityWithRandomPk());
        assertEquals(2, dao.count());
    }

    public void testInsertTwice() {
        K pk = nextPk();
        T entity = createEntity(pk);
        dao.insert(entity);
        try {
            dao.insert(entity);
            fail("Inserting twice should not work");
        } catch (SQLException expected) {
            // OK
        }
    }

    public void testInsertOrReplaceTwice() {
        T entity = createEntityWithRandomPk();
        long rowId1 = dao.insert(entity);
        long rowId2 = dao.insertOrReplace(entity);
        assertEquals(rowId1, rowId2);
    }

    public void testInsertOrReplaceInTx() {
        dao.deleteAll();
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < 20; i++) {
            list.add(createEntityWithRandomPk());
        }
        dao.insertOrReplaceInTx(list);
        dao.insertOrReplaceInTx(list);
        assertEquals(list.size(), dao.count());
    }

    public void testDelete() {
        K pk = nextPk();
        dao.deleteByKey(pk);
        T entity = createEntity(pk);
        dao.insert(entity);
        assertNotNull(dao.load(pk));
        dao.deleteByKey(pk);
        assertNull(dao.load(pk));
    }

    public void testDeleteAll() {
        List<T> entityList = new ArrayList<T>();
        for (int i = 0; i < 10; i++) {
            T entity = createEntityWithRandomPk();
            entityList.add(entity);
        }
        dao.insertInTx(entityList);
        dao.deleteAll();
        assertEquals(0, dao.count());
        for (T entity : entityList) {
            K key = daoAccess.getKey(entity);
            assertNotNull(key);
            assertNull(dao.load(key));
        }
    }

    public void testRowId() {
        T entity1 = createEntityWithRandomPk();
        T entity2 = createEntityWithRandomPk();
        long rowId1 = dao.insert(entity1);
        long rowId2 = dao.insert(entity2);
        assertTrue(rowId1 != rowId2);
    }

    public void testLoadAll() {
        dao.deleteAll();
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < 15; i++) {
            T entity = createEntity(nextPk());
            list.add(entity);
        }
        dao.insertInTx(list);
        List<T> loaded = dao.loadAll();
        assertEquals(list.size(), loaded.size());
    }

    public void testQuery() {
        dao.insert(createEntityWithRandomPk());
        K pkForQuery = nextPk();
        dao.insert(createEntity(pkForQuery));
        dao.insert(createEntityWithRandomPk());

        String where = "WHERE " + dao.getPkColumns()[0] + "=?";
        List<T> list = dao.queryRaw(where, pkForQuery.toString());
        assertEquals(1, list.size());
        assertEquals(pkForQuery, daoAccess.getKey(list.get(0)));
    }

    public void testUpdate() {
        dao.deleteAll();
        T entity = createEntityWithRandomPk();
        dao.insert(entity);
        dao.update(entity);
        assertEquals(1, dao.count());
    }

    public void testReadWithOffset() {
        K pk = nextPk();
        T entity = createEntity(pk);
        dao.insert(entity);

        Cursor cursor = queryWithDummyColumnsInFront(5, "42", pk);
        try {
            T entity2 = daoAccess.readEntity(cursor, 5);
            assertEquals(pk, daoAccess.getKey(entity2));
        } finally {
            cursor.close();
        }
    }

    public void testLoadPkWithOffset() {
        runLoadPkTest(10);
    }

    public void testLoadPk() {
        runLoadPkTest(0);
    }

    protected void runLoadPkTest(int offset) {
        K pk = nextPk();
        T entity = createEntity(pk);
        dao.insert(entity);

        Cursor cursor = queryWithDummyColumnsInFront(offset, "42", pk);
        try {
            K pk2 = daoAccess.readKey(cursor, offset);
            assertEquals(pk, pk2);
        } finally {
            cursor.close();
        }
    }

    protected Cursor queryWithDummyColumnsInFront(int dummyCount, String valueForColumn, K pk) {
        StringBuilder builder = new StringBuilder("SELECT ");
        for (int i = 0; i < dummyCount; i++) {
            builder.append(valueForColumn).append(",");
        }
        SqlUtils.appendColumns(builder, "T", dao.getAllColumns()).append(" FROM ");
        builder.append(dao.getTablename()).append(" T");
        if (pk != null) {
            builder.append(" WHERE ");

            assertEquals(1, dao.getPkColumns().length);
            builder.append(dao.getPkColumns()[0]).append("=");
            DatabaseUtils.appendValueToSql(builder, pk);
        }

        String select = builder.toString();
        Cursor cursor = db.rawQuery(select, null);
        assertTrue(cursor.moveToFirst());
        try {
            for (int i = 0; i < dummyCount; i++) {
                assertEquals(valueForColumn, cursor.getString(i));
            }
            if (pk != null) {
                assertEquals(1, cursor.getCount());
            }
        } catch (RuntimeException ex) {
            cursor.close();
            throw ex;
        }
        return cursor;
    }

    protected K nextPk() {
        for (int i = 0; i < 100000; i++) {
            K pk = createRandomPk();
            if (usedPks.add(pk)) {
                return pk;
            }
        }
        throw new IllegalStateException("Could not find a new PK");
    }

    protected T createEntityWithRandomPk() {
        return createEntity(createRandomPk());
    }

    /** K does not have to be collision free, check nextPk for collision free PKs. */
    protected abstract K createRandomPk();

    /**
     * Creates an insertable entity. If the given key is null, but the entity's PK is not null the method must return
     * null.
     */
    protected abstract T createEntity(K key);

}