package com.orientechnologies.orient.server.jwt.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orientechnologies.orient.core.metadata.security.jwt.OJwtPayload;

/**
 * Created by emrul on 28/09/2014.
 *
 * @author Emrul Islam <emrul@emrul.com> Copyright 2014 Emrul Islam
 */
public class OrientJwtPayload implements OJwtPayload {
  public String iss;
  public String sub;
  public String aud;
  public String jti;
  public long   exp;
  public long   iat;
  public long   nbf;
  public String userRid;
  public String database;
  public String databaseType;

  @Override
  public String getIssuer() {
    return iss;
  }

  @Override
  public void setIssuer(String iss) {
    this.iss = iss;
  }

  @Override
  public long getExpiry() {
    return exp;
  }

  @Override
  public void setExpiry(long exp) {
    this.exp = exp;
  }

  @Override
  public long getIssuedAt() {
    return iat;
  }

  @Override
  public void setIssuedAt(long iat) {
    this.iat = iat;
  }

  @Override
  public long getNotBefore() {
    return nbf;
  }

  @Override
  public void setNotBefore(long nbf) {
    this.nbf = nbf;
  }

  @Override
  public String getUserName() {
    return sub;
  }

  @Override
  public void setUserName(String sub) {
    this.sub = sub;
  }

  @Override
  public String getAudience() {
    return aud;
  }

  @Override
  public void setAudience(String aud) {
    this.aud = aud;
  }

  @Override
  public String getTokenId() {
    return jti;
  }

  @Override
  public void setTokenId(String jti) {
    this.jti = jti;
  }

  @JsonProperty(value = "userRid")
  public String getUserRid() {
    return userRid;
  }

  @JsonProperty(value = "userRid")
  public void setUserRid(String userRid) {
    this.userRid = userRid;
  }

  @JsonProperty(value = "dbName")
  public String getDatabase() {
    return database;
  }

  @JsonProperty(value = "dbName")
  public void setDatabase(String dbName) {
    this.database = dbName;
  }

  @Override
  public String getDatabaseType() {
    return databaseType;
  }

  @Override
  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

}