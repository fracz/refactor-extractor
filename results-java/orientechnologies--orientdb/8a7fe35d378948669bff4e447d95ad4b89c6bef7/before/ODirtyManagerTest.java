package com.orientechnologies.orient.core.db.record.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODirtyManager;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ODocumentInternal;

public class ODirtyManagerTest {

  public ODirtyManagerTest() {
  }

  @Test
  public void testBasic() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(1, manager.getNewRecord().size());
  }

  @Test
  public void testEmbeddedDocument() {
    ODocument doc = new ODocument();
    ODocument doc1 = new ODocument();
    doc.field("test", doc1, OType.EMBEDDED);
    ODocument doc2 = new ODocument();
    doc1.field("test2", doc2);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertEquals(doc2, manager.getPointed(doc).get(0));
  }

  @Test
  public void testLink() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    ODocument doc2 = new ODocument();
    doc.field("test1", doc2);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertEquals(doc2, manager.getPointed(doc).get(0));
  }

  @Test
  public void testLinkOther() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    ODocument doc1 = new ODocument();
    doc.field("test1", doc1);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc1);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertEquals(doc1, manager.getPointed(doc).get(0));
  }

  @Test
  public void testLinkCollection() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument doc1 = new ODocument();
    lst.add(doc1);
    doc.field("list", lst);

    Set<ODocument> set = new HashSet<ODocument>();
    ODocument doc2 = new ODocument();
    set.add(doc2);
    doc.field("set", set);

    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(3, manager.getNewRecord().size());
    assertEquals(2, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(doc1));
    assertTrue(manager.getPointed(doc).contains(doc2));
  }

  @Test
  public void testLinkCollectionOther() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument doc1 = new ODocument();
    lst.add(doc1);
    doc.field("list", lst);
    Set<ODocument> set = new HashSet<ODocument>();
    ODocument doc2 = new ODocument();
    set.add(doc2);
    doc.field("set", set);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc1);
    ODirtyManager manager2 = ORecordInternal.getDirtyManager(doc2);
    assertTrue(manager2.isSame(manager));
    assertEquals(3, manager.getNewRecord().size());
  }

  @Test
  public void testLinkMapOther() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    Map<String, ODocument> map = new HashMap<String, ODocument>();
    ODocument doc1 = new ODocument();
    map.put("some", doc1);
    doc.field("list", map);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc1);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(doc1));
  }

  @Test
  public void testEmbeddedMap() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    Map<String, Object> map = new HashMap<String, Object>();

    ODocument doc1 = new ODocument();
    map.put("bla", "bla");
    map.put("some", doc1);

    doc.field("list", map, OType.EMBEDDEDMAP);

    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(1, manager.getNewRecord().size());
  }

  @Test
  public void testEmbeddedCollection() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");

    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument doc1 = new ODocument();
    lst.add(doc1);
    doc.field("list", lst, OType.EMBEDDEDLIST);

    Set<ODocument> set = new HashSet<ODocument>();
    ODocument doc2 = new ODocument();
    set.add(doc2);
    doc.field("set", set, OType.EMBEDDEDSET);

    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(1, manager.getNewRecord().size());
  }

  @Test
  public void testRidBagOther() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    ORidBag bag = new ORidBag();
    ODocument doc1 = new ODocument();
    bag.add(doc1);
    doc.field("bag", bag);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc1);
    assertEquals(2, manager.getNewRecord().size());
  }

  @Test
  public void testEmbendedWithEmbeddedCollection() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");

    ODocument emb = new ODocument();
    doc.field("emb", emb, OType.EMBEDDED);

    ODocument embedInList = new ODocument();
    List<ODocument> lst = new ArrayList<ODocument>();
    lst.add(embedInList);
    emb.field("lst", lst, OType.EMBEDDEDLIST);
    ODocument link = new ODocument();
    embedInList.field("set", link);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);

    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(link));
  }

  @Test
  public void testDoubleLevelEmbeddedCollection() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument embeddedInList = new ODocument();
    ODocument link = new ODocument();
    embeddedInList.field("link", link);
    lst.add(embeddedInList);
    Set<ODocument> set = new HashSet<ODocument>();
    ODocument embeddedInSet = new ODocument();
    embeddedInSet.field("list", lst, OType.EMBEDDEDLIST);
    set.add(embeddedInSet);
    doc.field("set", set, OType.EMBEDDEDSET);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    ODirtyManager managerNested = ORecordInternal.getDirtyManager(embeddedInSet);
    assertTrue(manager.isSame(managerNested));
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(link));
  }

  @Test
  public void testDoubleCollectionEmbedded() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument embeddedInList = new ODocument();
    ODocument link = new ODocument();
    embeddedInList.field("link", link);
    lst.add(embeddedInList);
    Set<Object> set = new HashSet<Object>();
    set.add(lst);
    doc.field("set", set, OType.EMBEDDEDSET);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(link));
  }

  @Test
  public void testDoubleCollectionDocumentEmbedded() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument embeddedInList = new ODocument();
    ODocument link = new ODocument();
    ODocument embInDoc = new ODocument();
    embInDoc.field("link", link);
    embeddedInList.field("some", embInDoc, OType.EMBEDDED);
    lst.add(embeddedInList);
    Set<Object> set = new HashSet<Object>();
    set.add(lst);
    doc.field("set", set, OType.EMBEDDEDSET);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(link));
  }

  @Test
  public void testDoubleMapEmbedded() {
    ODocument doc = new ODocument();
    doc.field("test", "ddd");
    List<ODocument> lst = new ArrayList<ODocument>();
    ODocument embeddedInList = new ODocument();
    ODocument link = new ODocument();
    embeddedInList.field("link", link);
    lst.add(embeddedInList);
    Map<String, Object> set = new HashMap<String, Object>();
    set.put("some", lst);
    doc.field("set", set, OType.EMBEDDEDMAP);
    ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
    ODirtyManager manager = ORecordInternal.getDirtyManager(doc);
    assertEquals(2, manager.getNewRecord().size());
    assertEquals(1, manager.getPointed(doc).size());
    assertTrue(manager.getPointed(doc).contains(link));
  }

}