package org.jetbrains.idea.maven.repository;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.codehaus.plexus.PlexusContainer;
import org.jetbrains.idea.maven.MavenTestCase;
import org.jetbrains.idea.maven.embedder.EmbedderFactory;
import org.sonatype.nexus.index.*;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.scan.ScanningResult;
import org.sonatype.nexus.index.updater.IndexUpdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NexusIndexerTest extends MavenTestCase {
  private MavenWithDataTestFixture myDataTestFixture;
  private MavenEmbedder embedder;
  private NexusIndexer indexer;
  private IndexUpdater updater;
  private File indexDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myDataTestFixture = new MavenWithDataTestFixture(new File(myTempDirFixture.getTempDirPath()));
    myDataTestFixture.setUp();

    embedder = EmbedderFactory.createEmbedderForExecute(getMavenCoreSettings());

    PlexusContainer p = embedder.getPlexusContainer();
    indexer = (NexusIndexer)p.lookup(NexusIndexer.class);
    updater = (IndexUpdater)p.lookup(IndexUpdater.class);

    assertNotNull(indexer);
    assertNotNull(updater);

    indexDir = new File(myDir, "index");
    assertNotNull(indexDir);
  }

  @Override
  protected void tearDown() throws Exception {
    for (IndexingContext c : indexer.getIndexingContexts().values()) {
      indexer.removeIndexingContext(c, false);
    }
    embedder.stop();
    super.tearDown();
  }

  public void testSeraching() throws Exception {
    addContext("local1", new File(myDataTestFixture.getTestDataPath("local1_index")), null, null);
    assertSearchWorks();
  }

  public void testCreating() throws Exception {
    IndexingContext c = addContext("local1", indexDir, new File(myDataTestFixture.getTestDataPath("local1")), null);
    indexer.scan(c, new NullScanningListener());

    assertSearchWorks();
  }

  public void testDownloading() throws Exception {
    IndexingContext c = addContext("remote", indexDir, null, "file:///" + myDataTestFixture.getTestDataPath("remote"));
    updater.fetchAndUpdateIndex(c, new NullTransferListener());

    assertSearchWorks();
  }

  public void testAddingArtifacts() throws Exception {
    IndexingContext c = addContext("virtual", indexDir, null, null);

    createProjectPom("");

    ArtifactInfo ai = new ArtifactInfo(c.getRepositoryId(), "group", "id", "version", null);
    ArtifactContext a = new ArtifactContext(new File(myProjectPom.getPath()), null, null, ai);

    indexer.addArtifactToIndex(a, c);

    Query q = new TermQuery(new Term(ArtifactInfo.GROUP_ID, "group"));
    Collection<ArtifactInfo> result = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, q);

    assertEquals(1, result.size());

    ArtifactInfo found = new ArrayList<ArtifactInfo>(result).get(0);
    assertEquals("group", found.groupId);
    assertEquals("id", found.artifactId);
    assertEquals("version", found.version);

    IndexReader r = c.getIndexReader();
    for (int i = 0; i < r.numDocs(); i++) {
      Document d = r.document(i);
    }
  }

  public void ignoreTestIteratingAddedArtifacts() throws Exception {
    IndexingContext c = addContext("virtual", indexDir, null, null);

    addArtifact(c, "group1", "id1", "version1", "x:/path1");
    addArtifact(c, "group2", "id2", "version2", "x:/path2");
    addArtifact(c, "group3", "id3", "version3", "x:/path3");

    IndexReader r = c.getIndexReader();
    assertEquals(5, r.numDocs());
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < r.numDocs(); i++) {
      // third document becomes deleted somehow...
      Document d = r.document(i);
      String uinfo = d.get(ArtifactInfo.UINFO);
      result.add(uinfo);
    }
    System.out.println(result);
  }

  private void addArtifact(IndexingContext c, String groupId, String artifactId, String version, String path) throws IOException {
    ArtifactInfo ai = new ArtifactInfo(c.getRepositoryId(), groupId, artifactId, version, null);
    ai.size = -1;
    ai.lastModified = -1;
    ai.sourcesExists = ArtifactAvailablility.fromString(Integer.toString(0));
    ai.javadocExists = ArtifactAvailablility.fromString(Integer.toString(0));

    ArtifactContext a = new ArtifactContext(new File(path), null, null, ai);
    indexer.addArtifactToIndex(a, c);
  }

  private IndexingContext addContext(String id, File indexDir, File repoDir, String repoUrl) throws Exception {
    return indexer.addIndexingContext(
        id,
        id,
        repoDir,
        indexDir,
        repoUrl,
        null, // repo update url
        NexusIndexer.FULL_INDEX);
  }

  private void assertSearchWorks() throws IOException, IndexContextInInconsistentStateException {
    WildcardQuery q = new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, "junit*"));
    Collection<ArtifactInfo> result = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, q);
    assertEquals(3, result.size());
  }

  private static class NullScanningListener implements ArtifactScanningListener {
    public void scanningStarted(IndexingContext indexingContext) {
    }

    public void scanningFinished(IndexingContext indexingContext, ScanningResult scanningResult) {
    }

    public void artifactError(ArtifactContext artifactContext, Exception e) {
    }

    public void artifactDiscovered(ArtifactContext artifactContext) {
    }
  }

  private static class NullTransferListener implements TransferListener {
    public void transferInitiated(TransferEvent event) {
    }

    public void transferStarted(TransferEvent event) {
    }

    public void transferProgress(TransferEvent event, byte[] bytes, int i) {
    }

    public void transferCompleted(TransferEvent event) {
    }

    public void transferError(TransferEvent event) {
    }

    public void debug(String s) {
    }
  }
}