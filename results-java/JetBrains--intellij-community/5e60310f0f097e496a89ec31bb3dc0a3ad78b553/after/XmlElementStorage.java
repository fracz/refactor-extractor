package com.intellij.openapi.components.impl.stores;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.StateStorage;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.TrackingPathMacroSubstitutor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.StreamProvider;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.StringInterner;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

abstract class XmlElementStorage implements StateStorage, Disposable {
  @NonNls private static final Set<String> OBSOLETE_COMPONENT_NAMES = new HashSet<String>(Arrays.asList(
    "Palette"
  ));
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.components.impl.stores.XmlElementStorage");

  @NonNls private static final String COMPONENT = "component";
  @NonNls private static final String ATTR_NAME = "name";
  @NonNls private static final String NAME = ATTR_NAME;

  protected TrackingPathMacroSubstitutor myPathMacroSubstitutor;
  private @NotNull final String myRootElementName;
  private Object mySession;
  private StorageData myLoadedData;
  protected static StringInterner ourInterner = new StringInterner();
  protected final StreamProvider myStreamProvider;
  protected final String myFileSpec;
  private final ComponentRoamingManager myComponentRoamingManager;
  private final boolean myIsProjectSettings;
  protected Integer myUpToDateHash;



  protected XmlElementStorage(
    @Nullable final TrackingPathMacroSubstitutor pathMacroSubstitutor,
    @NotNull Disposable parentDisposable,
    @NotNull String rootElementName,
    StreamProvider streamProvider,
    String fileSpec, ComponentRoamingManager componentRoamingManager) {
    myPathMacroSubstitutor = pathMacroSubstitutor;
    myRootElementName = rootElementName;
    myStreamProvider = streamProvider;
    myFileSpec = fileSpec;
    myComponentRoamingManager = componentRoamingManager;
    Disposer.register(parentDisposable, this);
    myIsProjectSettings = "$PROJECT_FILE$".equals(myFileSpec) || myFileSpec.startsWith("$PROJECT_CONFIG_DIR$");
  }

  @Nullable
  protected abstract Document loadDocument() throws StateStorage.StateStorageException;

  @Nullable
  protected synchronized Element getState(final String componentName) throws StateStorage.StateStorageException {
    final StorageData storageData = getStorageData();
    final Element state = storageData.getState(componentName);

    if (state != null) {
      storageData.removeState(componentName);
    }

    return state;
  }

  public boolean hasState(final Object component, final String componentName, final Class<?> aClass) throws StateStorageException {
    final StorageData storageData = getStorageData();
    return storageData.hasState(componentName);
  }

  @Nullable
  public <T> T getState(final Object component, final String componentName, Class<T> stateClass, @Nullable T mergeInto) throws StateStorageException {
    final Element element = getState(componentName);
    return DefaultStateSerializer.deserializeState(element, stateClass, mergeInto);
  }

  @NotNull
  protected StorageData getStorageData() throws StateStorageException {
    if (myLoadedData != null) return myLoadedData;

    myLoadedData = loadData();

    return myLoadedData;
  }

  @NotNull
  private StorageData loadData() throws StateStorageException {
    StorageData result = createStorageData();

    Document document = loadDocument();

    if (document != null) {
      loadState(result, document.getRootElement());
    }

    if (!myIsProjectSettings) {
      for (RoamingType roamingType : RoamingType.values()) {
        if (roamingType != RoamingType.DISABLED) {
          try {
            final Document sharedDocument = StorageUtil.loadDocument(myStreamProvider.loadContent(myFileSpec, roamingType));

            if (sharedDocument != null) {
              filterComponentsDisabledForRoaming(sharedDocument.getRootElement(), roamingType);

              loadState(result, sharedDocument.getRootElement());
            }
          }
          catch (Exception e) {
            LOG.warn(e);
          }
        }

      }
    }


    return result;
  }

  protected void loadState(final StorageData result, final Element element) throws StateStorageException {

    if (myPathMacroSubstitutor != null) {
      myPathMacroSubstitutor.expandPaths(element);
    }

    JDOMUtil.internElement(element, ourInterner);

    try {
      result.load(element);
    }
    catch (IOException e) {
      throw new StateStorageException(e);
    }
  }

  @NotNull
  protected StorageData createStorageData() {
    return new StorageData(myRootElementName);
  }

  public void setDefaultState(final Element element) {
    myLoadedData = createStorageData();
    try {
      loadState(myLoadedData, element);
    }
    catch (StateStorageException e) {
      LOG.error(e);
    }
  }

  @NotNull
  public ExternalizationSession startExternalization() {
    try {
      final ExternalizationSession session = new MyExternalizationSession(getStorageData().clone());

      mySession = session;
      return session;
    }
    catch (StateStorageException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  public SaveSession startSave(final ExternalizationSession externalizationSession) {
    assert mySession == externalizationSession;

    final SaveSession saveSession = createSaveSession((MyExternalizationSession)externalizationSession);
    mySession = saveSession;
    return saveSession;
  }

  protected abstract MySaveSession createSaveSession(final MyExternalizationSession externalizationSession);

  public void finishSave(final SaveSession saveSession) {
    assert mySession == saveSession;
    mySession = null;
  }

  protected class MyExternalizationSession implements ExternalizationSession {
    private StorageData myStorageData;

    public MyExternalizationSession(final StorageData storageData) {
      myStorageData = storageData;
    }

    public void setState(final Object component, final String componentName, final Object state, final Storage storageSpec) throws StateStorageException {
      assert mySession == this;

      try {
        setState(componentName, DefaultStateSerializer.serializeState(state, storageSpec));
      }
      catch (WriteExternalException e) {
        LOG.debug(e);
      }
    }

    private synchronized void setState(final String componentName, final Element element)  {
      if (element.getAttributes().isEmpty() && element.getChildren().isEmpty()) return;

      myStorageData.setState(componentName, element);
    }
  }

  protected abstract class MySaveSession implements SaveSession {
    StorageData myStorageData;
    private Document myDocumentToSave;
    private Set<String> myUsedMacros;

    public MySaveSession(MyExternalizationSession externalizationSession) {
      myStorageData = externalizationSession.myStorageData;

    }

    public final boolean needsSave() throws StateStorageException {
      assert mySession == this;
      return _needsSave(calcHash());
    }

    private boolean _needsSave(final Integer hash) {
      if (myUpToDateHash == null) {
        if (hash != null) {
          if (!phisicalContentNeedsSave()) {
            myUpToDateHash = hash;
            return false;
          }
          else {
            return true;
          }
        }
        else {
          return true;
        }
      }
      else {
        if (hash != null) {
          if (hash.intValue() == myUpToDateHash.intValue()) {
            return false;
          }
          if (!phisicalContentNeedsSave()) {
            myUpToDateHash = hash;
            return false;
          }
          else {
            return true;
          }

        }
        else {
          return phisicalContentNeedsSave();
        }

      }
    }

    protected boolean phisicalContentNeedsSave() {
      return true;
    }

    protected abstract void doSave() throws StateStorage.StateStorageException;

    public void clearHash() {
      myUpToDateHash = null;
    }


    protected Integer calcHash() {
      return null;
    }

    public final void save() throws StateStorageException {
      assert mySession == this;

      Integer hash = calcHash();
      try {
        if (!isHashUpToDate(hash)) {
          try {
            if (!myIsProjectSettings) {
              for (RoamingType roamingType : RoamingType.values()) {
                if (roamingType != RoamingType.DISABLED) {
                  try {
                    Document copy = (Document)getDocumentToSave().clone();
                    filterComponentsDisabledForRoaming(copy.getRootElement(), roamingType);

                    if (copy.getRootElement().getChildren().size() > 0) {
                      myStreamProvider.saveContent(myFileSpec, StorageUtil.printDocument(copy), roamingType);
                    }
                  }
                  catch (IOException e) {
                    LOG.warn(e);
                  }
                }

              }
            }

          }
          finally {
            if (_needsSave(hash)) {
              doSave();
            }
          }

        }
      }
      finally {
        myUpToDateHash = hash;
      }


    }

    private boolean isHashUpToDate(final Integer hash) {
      return myUpToDateHash != null && myUpToDateHash.equals(hash);
    }

    public boolean isHashUpToDate() {
      return isHashUpToDate(calcHash());
    }

    public Set<String> getUsedMacros() {
      assert mySession == this;

      if (myUsedMacros == null) {
        if (myPathMacroSubstitutor != null) {
          myPathMacroSubstitutor.reset();
          final Map<String, Element> states = myStorageData.myComponentStates;

          for (Element e : states.values()) {
            myPathMacroSubstitutor.collapsePaths((Element)e.clone());
          }

          myUsedMacros = new HashSet<String>(myPathMacroSubstitutor.getUsedMacros());
        }
        else {
          myUsedMacros = new HashSet<String>();
        }
      }

      return myUsedMacros;
    }

    protected Document getDocumentToSave()  {
      if (myDocumentToSave != null) return myDocumentToSave;

      final Element element = myStorageData.save();
      myDocumentToSave = new Document(element);

      if (myPathMacroSubstitutor != null) {
        myPathMacroSubstitutor.reset();
        myPathMacroSubstitutor.collapsePaths(element);
      }

      return myDocumentToSave;
    }

    public StorageData getData() {
      return myStorageData;
    }

    @Nullable
    public Set<String> analyzeExternalChanges(final Set<Pair<VirtualFile,StateStorage>> changedFiles) {
      try {
        final StorageData storageData = loadData();

        return storageData.getDifference(myStorageData);
      }
      catch (StateStorageException e) {
        LOG.info(e);
      }

      return null;
    }
  }

  public void dispose() {
  }

  protected static class StorageData {
    private final Map<String, Element> myComponentStates;
    protected final String myRootElementName;
    private Integer myHash;

    public StorageData(final String rootElementName) {
      myComponentStates = new TreeMap<String, Element>();
      myRootElementName = rootElementName;
    }

    protected StorageData(StorageData storageData) {
      myRootElementName = storageData.myRootElementName;
      myComponentStates = new TreeMap<String, Element>(storageData.myComponentStates);
    }

    protected void load(@NotNull Element rootElement) throws IOException {
      final Element[] elements = JDOMUtil.getElements(rootElement);
      for (Element element : elements) {
        if (element.getName().equals(COMPONENT)) {
          final String name = element.getAttributeValue(NAME);

          if (name == null) {
            LOG.error("Broken content in file : " + this);
            continue;
          }

          if (OBSOLETE_COMPONENT_NAMES.contains(name)) continue;

          element.detach();

          if (element.getAttributes().size() > 1 || !element.getChildren().isEmpty()) {
            assert element.getAttributeValue(NAME) != null : "No name attribute for component: " + name + " in " + this;
            myComponentStates.put(name, element);
          }
        }
      }
    }

    @NotNull
    protected Element save() {
      Element rootElement = new Element(myRootElementName);

      for (String componentName : myComponentStates.keySet()) {
        assert componentName != null;
        final Element element = myComponentStates.get(componentName);

        if (element.getAttribute(NAME) == null) element.setAttribute(NAME, componentName);

        rootElement.addContent((Element)element.clone());
      }

      return rootElement;
    }

    @Nullable
    public Element getState(final String name) {
      final Element e = myComponentStates.get(name);

      if (e != null) {
        assert e.getAttributeValue(NAME) != null : "No name attribute for component: " + name + " in " + this;
        e.removeAttribute(NAME);
      }

      return e;
    }

    public void removeState(final String componentName) {
      myComponentStates.remove(componentName);
      clearHash();
    }

    private void setState(@NotNull final String componentName, final Element element) {
      element.setName(COMPONENT);

      //componentName should be first!
      final List attributes = new ArrayList(element.getAttributes());
      for (Object attribute : attributes) {
        Attribute attr = (Attribute)attribute;
        element.removeAttribute(attr);
      }

      element.setAttribute(NAME, componentName);

      for (Object attribute : attributes) {
        Attribute attr = (Attribute)attribute;
        element.setAttribute(attr.getName(), attr.getValue());
      }

      myComponentStates.put(componentName, element);
      clearHash();
    }

    public StorageData clone() {
      return new StorageData(this);
    }

    public final int getHash() {
      if (myHash == null) {
        myHash = computeHash();
      }
      return myHash.intValue();
    }

    protected int computeHash() {
      int result = 0;

      for (String name : myComponentStates.keySet()) {
        result = 31*result + name.hashCode();
        result = 31*result + JDOMUtil.getTreeHash(myComponentStates.get(name));
      }

      return result;
    }

    protected void clearHash() {
      myHash = null;
    }

    public Set<String> getDifference(final StorageData storageData) {
      Set<String> bothStates = new HashSet<String>(myComponentStates.keySet());
      bothStates.retainAll(storageData.myComponentStates.keySet());

      Set<String> diffs = new HashSet<String>();
      diffs.addAll(storageData.myComponentStates.keySet());
      diffs.addAll(myComponentStates.keySet());
      diffs.removeAll(bothStates);

      for (String componentName : bothStates) {
        final Element e1 = myComponentStates.get(componentName);
        final Element e2 = storageData.myComponentStates.get(componentName);

        if (!JDOMUtil.areElementsEqual(e1, e2)) {
          diffs.add(componentName);
        }
      }


      return diffs;
    }

    public boolean hasState(final String componentName) {
        return myComponentStates.containsKey(componentName);
    }

  }

  public void reload(@NotNull final Set<String> changedComponents) throws StateStorageException {
    final StorageData storageData = loadData();

    final StorageData oldLoadedData = myLoadedData;

    if (oldLoadedData != null) {
      Set<String> componentsToRetain = new HashSet<String>(oldLoadedData.myComponentStates.keySet());
      componentsToRetain.addAll(changedComponents);
      storageData.myComponentStates.keySet().retainAll(componentsToRetain);
    }

    myLoadedData = storageData;
  }

  private void filterComponentsDisabledForRoaming(final Element element, final RoamingType roamingType) {
    final List components = element.getChildren(COMPONENT);

    List<Element> toDelete = new ArrayList<Element>();

    for (Object componentObj : components) {
      final Element componentElement = (Element)componentObj;
      final String nameAttr = componentElement.getAttributeValue(NAME);

      if (myComponentRoamingManager.getRoamingType(nameAttr) != roamingType) {
        toDelete.add(componentElement);
      }
    }

    for (Element toDeleteElement : toDelete) {
      element.removeContent(toDeleteElement);
    }
  }


}