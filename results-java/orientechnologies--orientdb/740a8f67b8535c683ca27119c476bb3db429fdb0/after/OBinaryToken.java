package com.orientechnologies.orient.server.binary.impl;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.metadata.security.jwt.OJwtHeader;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class OBinaryToken implements OToken {

  private boolean    valid;
  private boolean    verified;
  private String     userName;
  private String     database;
  private long       expiry;
  private ORID       rid;
  private String     databaseType;
  private OJwtHeader header;

  @Override
  public boolean getIsVerified() {
    return verified;
  }

  @Override
  public void setIsVerified(boolean verified) {
    this.verified = verified;
  }

  @Override
  public boolean getIsValid() {
    return valid;
  }

  @Override
  public void setIsValid(boolean valid) {
    this.valid = valid;
  }

  @Override
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Override
  public OUser getUser(ODatabaseDocumentInternal db) {
    ODocument result = db.load(new ORecordId(this.rid), "roles:1");
    if (!result.getClassName().equals(OUser.CLASS_NAME)) {
      result = null;
    }
    return new OUser(result);
  }

  @Override
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  @Override
  public String getDatabaseType() {
    return databaseType;
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

  @Override
  public ORID getUserId() {
    return rid;
  }

  public void setUserRid(ORID rid) {
    this.rid = rid;
  }

  public OJwtHeader getHeader() {
    return header;
  }

  public void setHeader(OJwtHeader header) {
    this.header = header;
  }

  @Override
  public long getExpiry() {
    return expiry;
  }

  public void setExpiry(long expiry) {
    this.expiry = expiry;
  }
}