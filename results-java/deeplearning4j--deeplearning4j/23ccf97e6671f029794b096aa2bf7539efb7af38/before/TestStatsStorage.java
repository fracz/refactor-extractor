package org.deeplearning4j.ui.storage;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.deeplearning4j.api.storage.Persistable;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.api.storage.StatsStorageEvent;
import org.deeplearning4j.api.storage.StatsStorageListener;
import org.deeplearning4j.ui.stats.api.StatsInitializationReport;
import org.deeplearning4j.ui.stats.api.StatsReport;
import org.deeplearning4j.ui.stats.impl.SbeStatsInitializationReport;
import org.deeplearning4j.ui.stats.impl.SbeStatsReport;
import org.deeplearning4j.ui.storage.mapdb.MapDBStatsStorage;
import org.deeplearning4j.ui.storage.sqlite.J7FileStatsStorage;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Alex on 03/10/2016.
 */
public class TestStatsStorage {


    @Test
    public void testStatsStorage() throws IOException {

        for( int i=0; i<3; i++ ) {

            StatsStorage ss;
            switch(i){
                case 0:
                    File f = Files.createTempFile("TestMapDbStatsStore", ".db").toFile();
                    f.delete(); //Don't want file to exist...
                    ss = new MapDBStatsStorage.Builder()
                            .file(f)
                            .build();
                    break;
                case 1:
                    File f2 = Files.createTempFile("TestJ7FileStatsStore", ".db").toFile();
                    f2.delete(); //Don't want file to exist...
                    ss = new J7FileStatsStorage(f2);
                    break;
                case 2:
                    ss = new InMemoryStatsStorage();
                    break;
                default:
                    throw new RuntimeException();
            }



            CountingListener l = new CountingListener();
            ss.registerStatsStorageListener(l);
            assertEquals(1, ss.getListeners().size());

            assertEquals(0, ss.listSessionIDs().size());
            assertNull(ss.getLatestUpdate("sessionID", "typeID", "workerID"));
            assertEquals(0, ss.listSessionIDs().size());


            ss.putStaticInfo(getInitReport(0));
            assertEquals(1, l.countNewSession);
            assertEquals(1, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(0, l.countUpdate);

            assertEquals(Collections.singletonList("sid0"), ss.listSessionIDs());
            assertTrue(ss.sessionExists("sid0"));
            assertFalse(ss.sessionExists("sid1"));
            Persistable expected = getInitReport(0);
            Persistable p = ss.getStaticInfo("sid0", "tid0", "wid0");
            assertEquals(expected, p);
            assertNull(ss.getLatestUpdate("sid0", "tid0", "wid0"));
            assertEquals(0, ss.getAllUpdatesAfter("sid0", "tid0", "wid0", 0).size());


            ss.putUpdate(getReport(0, 0, 0, 12345));
            assertEquals(1, ss.getNumUpdateRecordsFor("sid0"));
            List<Persistable> list = ss.getLatestUpdateAllWorkers("sid0", "tid0");
            assertEquals(1, list.size());
            assertEquals(getReport(0, 0, 0, 12345), ss.getUpdate("sid0", "tid0", "wid0", 12345));
            assertEquals(1, l.countNewSession);
            assertEquals(1, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(1, l.countUpdate);

            ss.putUpdate(getReport(0, 0, 0, 12346));
            assertEquals(1, ss.getLatestUpdateAllWorkers("sid0", "tid0").size());
            assertEquals(getReport(0, 0, 0, 12346), ss.getLatestUpdate("sid0", "tid0", "wid0"));
            assertEquals(getReport(0, 0, 0, 12346), ss.getUpdate("sid0", "tid0", "wid0", 12346));

            ss.putUpdate(getReport(0, 0, 1, 12345));
            assertEquals(2, ss.getLatestUpdateAllWorkers("sid0", "tid0").size());
            assertEquals(getReport(0, 0, 1, 12345), ss.getLatestUpdate("sid0", "tid0", "wid1"));
            assertEquals(getReport(0, 0, 1, 12345), ss.getUpdate("sid0", "tid0", "wid1", 12345));

            assertEquals(1, l.countNewSession);
            assertEquals(2, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(3, l.countUpdate);

        }
    }


    @Test
    public void testFileStatsStore() throws IOException {

        for( int i=0; i<2; i++ ) {
            File f;
            if(i == 0){
                f = Files.createTempFile("TestMapDbStatsStore", ".db").toFile();
            } else {
                f = Files.createTempFile("TestSqliteStatsStore", ".db").toFile();
            }

            f.delete(); //Don't want file to exist...
            StatsStorage ss;
            if(i == 0){
                ss = new MapDBStatsStorage.Builder()
                        .file(f)
                        .build();
            } else {
                ss = new J7FileStatsStorage(f);
            }


            CountingListener l = new CountingListener();
            ss.registerStatsStorageListener(l);
            assertEquals(1, ss.getListeners().size());

            assertEquals(0, ss.listSessionIDs().size());
            assertNull(ss.getLatestUpdate("sessionID", "typeID", "workerID"));
            assertEquals(0, ss.listSessionIDs().size());


            ss.putStaticInfo(getInitReport(0));
            assertEquals(1, l.countNewSession);
            assertEquals(1, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(0, l.countUpdate);

            assertEquals(Collections.singletonList("sid0"), ss.listSessionIDs());
            assertTrue(ss.sessionExists("sid0"));
            assertFalse(ss.sessionExists("sid1"));
            Persistable expected = getInitReport(0);
            Persistable p = ss.getStaticInfo("sid0", "tid0", "wid0");
            assertEquals(expected, p);
            assertNull(ss.getLatestUpdate("sid0", "tid0", "wid0"));
            assertEquals(0, ss.getAllUpdatesAfter("sid0", "tid0", "wid0", 0).size());


            ss.putUpdate(getReport(0, 0, 0, 12345));
            assertEquals(1, ss.getNumUpdateRecordsFor("sid0"));
            List<Persistable> list = ss.getLatestUpdateAllWorkers("sid0", "tid0");
            assertEquals(1, list.size());
            assertEquals(getReport(0, 0, 0, 12345), ss.getUpdate("sid0", "tid0", "wid0", 12345));
            assertEquals(1, l.countNewSession);
            assertEquals(1, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(1, l.countUpdate);

            ss.putUpdate(getReport(0, 0, 0, 12346));
            assertEquals(1, ss.getLatestUpdateAllWorkers("sid0", "tid0").size());
            assertEquals(getReport(0, 0, 0, 12346), ss.getLatestUpdate("sid0", "tid0", "wid0"));
            assertEquals(getReport(0, 0, 0, 12346), ss.getUpdate("sid0", "tid0", "wid0", 12346));

            ss.putUpdate(getReport(0, 0, 1, 12345));
            assertEquals(2, ss.getLatestUpdateAllWorkers("sid0", "tid0").size());
            assertEquals(getReport(0, 0, 1, 12345), ss.getLatestUpdate("sid0", "tid0", "wid1"));
            assertEquals(getReport(0, 0, 1, 12345), ss.getUpdate("sid0", "tid0", "wid1", 12345));

            assertEquals(1, l.countNewSession);
            assertEquals(2, l.countNewWorkerId);
            assertEquals(1, l.countStaticInfo);
            assertEquals(3, l.countUpdate);


            //Close and re-open
            ss.close();
            assertTrue(ss.isClosed());

            if(i == 0){
                ss = new MapDBStatsStorage.Builder()
                        .file(f)
                        .build();
            } else {
                ss = new J7FileStatsStorage(f);
            }


            assertEquals(getReport(0, 0, 0, 12345), ss.getUpdate("sid0", "tid0", "wid0", 12345));
            assertEquals(getReport(0, 0, 0, 12346), ss.getLatestUpdate("sid0", "tid0", "wid0"));
            assertEquals(getReport(0, 0, 0, 12346), ss.getUpdate("sid0", "tid0", "wid0", 12346));
            assertEquals(getReport(0, 0, 1, 12345), ss.getLatestUpdate("sid0", "tid0", "wid1"));
            assertEquals(getReport(0, 0, 1, 12345), ss.getUpdate("sid0", "tid0", "wid1", 12345));
            assertEquals(2, ss.getLatestUpdateAllWorkers("sid0", "tid0").size());
        }
    }

    private static StatsInitializationReport getInitReport(int idNumber){
        StatsInitializationReport rep = new SbeStatsInitializationReport();
        rep.reportModelInfo("classname","jsonconfig",new String[]{"p0","p1"},1,10);
        rep.reportIDs("sid"+idNumber,"tid"+idNumber,"wid"+idNumber,12345);
        rep.reportHardwareInfo(0,2,1000,2000,new long[]{3000,4000},new String[]{"dev0","dev1"},"hardwareuid");
        Map<String,String> envInfo = new HashMap<>();
        envInfo.put("envInfo0","value0");
        envInfo.put("envInfo1", "value1");
        rep.reportSoftwareInfo("arch","osName","jvmName","jvmVersion","1.8","backend","dtype","hostname","jvmuid", envInfo);
        return rep;
    }

    private static StatsReport getReport(int sid, int tid, int wid, long time){
        StatsReport rep = new SbeStatsReport();
        rep.reportIDs("sid"+sid,"tid"+tid,"wid"+wid,time);
        rep.reportScore(100.0);
        rep.reportPerformance(1000,1001,1002,1003.0,1004.0);
        return rep;
    }

    @NoArgsConstructor @Data
    private static class CountingListener implements StatsStorageListener {

        private int countNewSession;
        private int countNewTypeID;
        private int countNewWorkerId;
        private int countStaticInfo;
        private int countUpdate;
        private int countMetaData;

        @Override
        public void notify(StatsStorageEvent event) {
            System.out.println("Event: " + event);
            switch (event.getEventType()){
                case NewSessionID:
                    countNewSession++;
                    break;
                case NewTypeID:
                    countNewTypeID++;
                    break;
                case NewWorkerID:
                    countNewWorkerId++;
                    break;
                case PostMetaData:
                    countMetaData++;
                    break;
                case PostStaticInfo:
                    countStaticInfo++;
                    break;
                case PostUpdate:
                    countUpdate++;
                    break;
            }
        }
    }


}