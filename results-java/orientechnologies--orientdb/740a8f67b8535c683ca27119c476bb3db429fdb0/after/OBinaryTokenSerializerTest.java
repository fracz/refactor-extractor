package com.orientechnologies.orient.server.jwt.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.server.binary.impl.OBinaryToken;

public class OBinaryTokenSerializerTest {

  private OBinaryTokenSerializer ser = new OBinaryTokenSerializer(new String[] { "plocal", "memory" }, new String[] { "key" },
                                         new String[] { "HmacSHA256" }, new String[] { "OrientDB" });

  @Test
  public void testSerializerDeserializeToken() throws IOException {
    OBinaryToken token = new OBinaryToken();
    token.setDatabase("test");
    token.setDatabaseType("plocal");
    token.setUserRid(new ORecordId(43, 234));
    OrientJwtHeader header = new OrientJwtHeader();
    header.setKeyId("key");
    header.setAlgorithm("HmacSHA256");
    header.setType("OrientDB");
    token.setHeader(header);
    token.setExpiry(20L);
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    ser.serialize(token, bas);
    ByteArrayInputStream input = new ByteArrayInputStream(bas.toByteArray());
    OBinaryToken tok = ser.deserialize(input);

    assertEquals("test", token.getDatabase());
    assertEquals("plocal", token.getDatabaseType());
    ORID id = token.getUserId();
    assertEquals(43, id.getClusterId());
    assertEquals(20L, tok.getExpiry());

    assertEquals("OrientDB", tok.getHeader().getType());
    assertEquals("HmacSHA256", tok.getHeader().getAlgorithm());
    assertEquals("key", tok.getHeader().getKeyId());

  }
}