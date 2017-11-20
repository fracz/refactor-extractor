package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luigidellaquila on 06/07/16.
 */
public class OResult {
  protected Map<String, Object> content = new HashMap<>();
  protected OIdentifiable element;

  public void setProperty(String name, Object value) {
    content.put(name, value);
  }

  public <T> T getProperty(String name) {
    if (element != null) {
      return ((ODocument) element.getRecord()).getProperty(name);
    }
    return (T)content.get(name);
  }

  public OIdentifiable getElement() {
    return element;
  }

  public void setElement(OIdentifiable element) {
    this.element = element;
  }

  @Override public String toString() {

    if(element!=null){
      return element.toString();
    }
    return
        "{\n"+
          content.entrySet().stream()
            .map(x -> x.getKey() + ": \n" + x.getValue())
            .reduce("", (a, b) -> a + b + "\n\n")
        +"}\n";

  }
}