package org.jetbrains.idea.maven.indices;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.jetbrains.idea.maven.MavenImportingTestCase;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class MavenIndicesTest extends MavenImportingTestCase {
  private MavenWithDataTestFixture myDataTestFixture;
  private MavenIndices myIndices;
  private MavenEmbedder myEmbedder;
  private File myIndexDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    myDataTestFixture = new MavenWithDataTestFixture(myDir);
    myDataTestFixture.setUp();

    initIndex();
  }

  @Override
  protected void tearDown() throws Exception {
    shutdownIndex();
    super.tearDown();
  }

  private void initIndex() throws Exception {
    initIndex("index");
  }

  private void initIndex(String relativeDir) {
    myEmbedder = MavenEmbedderFactory.createEmbedderForExecute(getMavenCoreSettings()).getEmbedder();
    myIndexDir = new File(myDir, relativeDir);
    myIndices = new MavenIndices(myEmbedder, myIndexDir);
  }

  private void shutdownIndex() throws MavenEmbedderException {
    myIndices.close();
    myEmbedder.stop();
  }

  public void testAddingLocal() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit");
  }

  public void testAddingSeveral() throws Exception {
    MavenIndex i1 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    MavenIndex i2 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    myIndices.add(i1);
    myIndices.add(i2);
    myIndices.update(i1, myProject, new EmptyProgressIndicator());
    myIndices.update(i2, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit", "jmock");
  }

  public void testAddingWithoutUpdate() throws Exception {
    myIndices.add(new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1")));
    assertTrue(myIndices.getGroupIds().isEmpty());
  }

  public void testUpdatingLocalClearsPreviousIndex() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);

    myIndices.update(i, myProject, new EmptyProgressIndicator());
    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit");

    myIndices.change(i, myDataTestFixture.getTestDataPath("local2"));
    myIndices.update(i, myProject, new EmptyProgressIndicator());
    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "jmock");
  }

  public void testChanging() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);

    myIndices.update(i, myProject, new EmptyProgressIndicator());
    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit");

    myIndices.change(i, myDataTestFixture.getTestDataPath("local2"));
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "jmock");
  }

  public void testAddingRemote() throws Exception {
    MavenIndex i = new RemoteMavenIndex("file:///" + myDataTestFixture.getTestDataPath("remote"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit");
  }

  public void testUpdatingRemote() throws Exception {
    // for some reason nexus holds 'timestamp' file and tearDown cannot delete it and fails.
    if (ignore()) return;

    MavenIndex i = new RemoteMavenIndex("file:///" + myDataTestFixture.getTestDataPath("remote"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    //shouldn't throw 'The existing index is for repository [remote] and not for repository [xxx]'
    myIndices.update(i, myProject, new EmptyProgressIndicator());
  }

  public void testChangingWithSameID() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    myIndices.change(i, myDataTestFixture.getTestDataPath("local2"));
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "jmock");
  }

  public void testRemoving() throws Exception {
    MavenIndex i = new RemoteMavenIndex("file:///" + myDataTestFixture.getTestDataPath("remote"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    myIndices.remove(i);
    assertTrue(myIndices.getGroupIds().isEmpty());
  }

  public void testClearIndexAfterRemoving() throws Exception {
    MavenIndex i = new RemoteMavenIndex("file:///" + myDataTestFixture.getTestDataPath("remote"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    myIndices.remove(i);
    myIndices.add(i);
    assertTrue(myIndices.getGroupIds().isEmpty());
  }

  public void testSaving() throws Exception {
    MavenIndex i1 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    MavenIndex i2 = new RemoteMavenIndex("file:///" + myDataTestFixture.getTestDataPath("remote"));
    myIndices.add(i1);
    myIndices.add(i2);
    myIndices.update(i1, myProject, new EmptyProgressIndicator());
    myIndices.update(i2, myProject, new EmptyProgressIndicator());

    myIndices.save();

    shutdownIndex();
    initIndex();
    myIndices.load();

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit", "jmock");
  }

  public void testSavingInAbsenseOfParentDirectories() throws Exception {
    String subDir = "subDir1/subDir2/index";
    initIndex(subDir);
    myIndices.save(); // shouldn't throw
  }

  public void testClearingIndexesOnLoadError() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    myIndices.add(i);
    myIndices.save();
    shutdownIndex();

    FileWriter w = new FileWriter(new File(myIndexDir, MavenIndices.INDICES_LIST_FILE));
    w.write("bad content");
    w.close();

    initIndex();

    myIndices.load();

    assertTrue(myIndices.getIndices().isEmpty());
    assertFalse(myIndexDir.exists());
  }

  public void testClearingAlreadyLoadedIndexesOnLoadError() throws Exception {
    myIndices.add(new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1")));
    myIndices.add(new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2")));
    myIndices.save();
    shutdownIndex();

    File listFile = new File(myIndexDir, MavenIndices.INDICES_LIST_FILE);
    byte[] bytes = FileUtil.loadFileBytes(listFile);
    FileOutputStream s = new FileOutputStream(listFile);
    s.write(bytes, 0, (int)(bytes.length / 1.5));
    s.close();

    initIndex();

    myIndices.load();

    assertTrue(myIndices.getIndices().isEmpty());
    assertFalse(myIndexDir.exists());
  }

  public void testLoadingIndexIfCachesAreBroken() throws Exception {
    MavenIndex i1 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    MavenIndex i2 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    myIndices.add(i1);
    myIndices.add(i2);
    myIndices.update(i1, myProject, new EmptyProgressIndicator());
    myIndices.update(i2, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit", "jmock");

    myIndices.save();
    shutdownIndex();

    File cachesDir = i1.getCurrentDataDir();
    File groupIds = new File(cachesDir, "groupIds.dat");
    assertTrue(groupIds.exists());

    FileWriter w = new FileWriter(groupIds);
    w.write("bad content");
    w.close();

    initIndex();

    myIndices.load();

    assertEquals(2, myIndices.getIndices().size());
    assertTrue(cachesDir.exists()); // recreated
    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "jmock");
  }

  public void testAddingIndexWithExistingDirectoryDoesNotThrowException() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    myIndices.save();
    shutdownIndex();

    initIndex();
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());
  }

  public void testGettingArtifactInfos() throws Exception {
    MavenIndex i1 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    MavenIndex i2 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    myIndices.add(i1);
    myIndices.add(i2);
    myIndices.update(i1, myProject, new EmptyProgressIndicator());
    myIndices.update(i2, myProject, new EmptyProgressIndicator());

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit", "jmock");

    assertUnorderedElementsAreEqual(myIndices.getArtifactIds("junit"), "junit");
    assertUnorderedElementsAreEqual(myIndices.getArtifactIds("jmock"), "jmock");
    assertUnorderedElementsAreEqual(myIndices.getArtifactIds("unknown"));

    assertUnorderedElementsAreEqual(myIndices.getVersions("junit", "junit"), "3.8.1", "3.8.2", "4.0");
    assertUnorderedElementsAreEqual(myIndices.getVersions("junit", "jmock"));
    assertUnorderedElementsAreEqual(myIndices.getVersions("unknown", "unknown"));
  }

  public void testGettingArtifactInfosFromUnUpdatedRepositories() throws Exception {
    myIndices.add(new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1")));
    assertUnorderedElementsAreEqual(myIndices.getGroupIds()); // shouldn't throw
  }

  public void testGettingArtifactInfosAfterReload() throws Exception {
    MavenIndex i = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    myIndices.add(i);
    myIndices.update(i, myProject, new EmptyProgressIndicator());

    myIndices.save();
    shutdownIndex();
    initIndex();
    myIndices.load();

    assertUnorderedElementsAreEqual(myIndices.getGroupIds(), "junit");
  }

  public void testHasArtifactInfo() throws Exception {
    MavenIndex i1 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local1"));
    MavenIndex i2 = new LocalMavenIndex(myDataTestFixture.getTestDataPath("local2"));
    myIndices.add(i1);
    myIndices.add(i2);
    myIndices.update(i1, myProject, new EmptyProgressIndicator());
    myIndices.update(i2, myProject, new EmptyProgressIndicator());

    assertTrue(myIndices.hasGroupId("junit"));
    assertTrue(myIndices.hasGroupId("jmock"));
    assertFalse(myIndices.hasGroupId("xxx"));

    assertTrue(myIndices.hasArtifactId("junit", "junit"));
    assertTrue(myIndices.hasArtifactId("jmock", "jmock"));
    assertFalse(myIndices.hasArtifactId("junit", "jmock"));

    assertTrue(myIndices.hasVersion("junit", "junit", "4.0"));
    assertTrue(myIndices.hasVersion("jmock", "jmock", "1.0.0"));
    assertFalse(myIndices.hasVersion("junit", "junit", "666"));
  }
}