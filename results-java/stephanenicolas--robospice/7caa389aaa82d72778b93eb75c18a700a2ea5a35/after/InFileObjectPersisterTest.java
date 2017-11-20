package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

@MediumTest
public class InFileObjectPersisterTest extends AbstractInFileObjectPersisterTest {

    InFileObjectPersister<Object> inFileObjectPersister;

    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
    private static final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";

    @Override
    protected void setUp() throws Exception {
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        inFileObjectPersister = new InFileObjectPersisterUnderTest(application);
        super.setUp(inFileObjectPersister);
    }

    public void testRemoveDataFromCache() throws Exception {
        super.testRemoveDataFromCache(new Object(), TEST_CACHE_KEY);
    }

    public void testRemoveAllDataFromCache() throws Exception {
        Map<Object, Object> mapDataToCacheKey = new HashMap<Object, Object>();
        mapDataToCacheKey.put(new Object(), TEST_CACHE_KEY);
        mapDataToCacheKey.put(new Object(), TEST_CACHE_KEY2);
        super.testRemoveAllDataFromCache(mapDataToCacheKey);
    }

    // ============================================================================================
    // CLASS UNDER TEST
    // ============================================================================================
    static class InFileObjectPersisterUnderTest extends InFileObjectPersister<Object> {
        InFileObjectPersisterUnderTest(Application application) {
            super(application, Object.class);
        }

        @Override
        public boolean canHandleClass(Class<?> arg0) {
            return false;
        }

        @Override
        public Object loadDataFromCache(Object arg0, long arg1) throws CacheLoadingException {
            return null;
        }

        @Override
        protected Object readCacheDataFromFile(File file) throws CacheLoadingException {
            return null;
        }

        @Override
        public Object saveDataToCacheAndReturnData(Object data, Object cacheKey) throws CacheSavingException {
            try {
                getCacheFile(cacheKey).createNewFile();
            } catch (IOException e) {
                throw new CacheSavingException(e);
            }
            return data;
        }
    }

}