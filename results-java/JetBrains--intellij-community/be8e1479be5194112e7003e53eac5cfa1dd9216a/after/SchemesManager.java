package com.intellij.openapi.options;

import com.intellij.openapi.util.WriteExternalException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.io.IOException;

public interface SchemesManager <T extends Scheme, E extends ExternalizableScheme>{
  SchemesManager EMPTY = new SchemesManager(){
    public Collection loadSchemes() {
      return Collections.emptySet();
    }

    public Collection loadSharedSchemes() {
      return Collections.emptySet();
    }

    public void exportScheme(final ExternalizableScheme scheme, final String name, final String description) throws WriteExternalException {
    }

    public boolean isImportAvailable() {
      return false;
    }

    public boolean isExportAvailable() {
      return false;
    }

    public boolean isShared(final Scheme scheme) {
      return false;
    }

    public void addNewScheme(final Scheme scheme, final boolean replaceExisting) {

    }

    public void clearAllSchemes() {
    }

    public List getAllSchemes() {
      return Collections.emptyList();
    }

    public Scheme findSchemeByName(final String schemeName) {
      return null;
    }

    public void save() {
    }

    public void setCurrentSchemeName(final String schemeName) {

    }

    public Scheme getCurrentScheme() {
      return null;
    }

    public void removeScheme(final Scheme scheme) {

    }

    public Collection getAllSchemeNames() {
      return Collections.emptySet();
    }

    public Collection loadSharedSchemes(final Collection currentSchemeList) {
      return loadSharedSchemes();
    }

    public File getRootDirectory() {
      return null;
    }
  };

  Collection<E> loadSchemes();

  Collection<SharedScheme<E>> loadSharedSchemes();
  Collection<SharedScheme<E>> loadSharedSchemes(Collection<T> currentSchemeList);

  void exportScheme(final E scheme, final String name, final String description) throws WriteExternalException, IOException;

  boolean isImportAvailable();

  boolean isExportAvailable();

  boolean isShared(final Scheme scheme);

  void addNewScheme(final T scheme, final boolean replaceExisting);

  void clearAllSchemes();

  List<T> getAllSchemes();

  @Nullable
  T findSchemeByName(final String schemeName);

  void save() throws WriteExternalException;

  void setCurrentSchemeName(final String schemeName);

  @Nullable
  T getCurrentScheme();

  void removeScheme(final T scheme);

  Collection<String> getAllSchemeNames();

  File getRootDirectory();
}