
package com.intellij.refactoring.migration;

import java.util.ArrayList;

/**
 *
 */
public class MigrationMap {
  private String myName;
  private String myDescription;
  private ArrayList<MigrationMapEntry> myEntries = new ArrayList<MigrationMapEntry>();

  public MigrationMap() {

  }

  public MigrationMap(MigrationMapEntry[] entries) {
    for (int i = 0; i < entries.length; i++) {
      MigrationMapEntry entry = entries[i];
      addEntry(entry);
    }
  }

  public MigrationMap cloneMap() {
    MigrationMap newMap = new MigrationMap();
    newMap.myName = myName;
    newMap.myDescription = myDescription;
    for(int i = 0; i < myEntries.size(); i++){
      MigrationMapEntry entry = getEntryAt(i);
      newMap.addEntry(entry.cloneEntry());
    }
    return newMap;
  }

  public String getName() {
    return myName;
  }

  public void setName(String name) {
    myName = name;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public void addEntry(MigrationMapEntry entry) {
    myEntries.add(entry);
  }

  public void removeEntryAt(int index) {
    myEntries.remove(index);
  }

  public void removeAllEntries() {
    myEntries.clear();
  }

  public int getEntryCount() {
    return myEntries.size();
  }

  public MigrationMapEntry getEntryAt(int index) {
    return myEntries.get(index);
  }

  public void setEntryAt(MigrationMapEntry entry, int index) {
    myEntries.set(index, entry);
  }

  public String toString() {
    return getName();
  }
}


