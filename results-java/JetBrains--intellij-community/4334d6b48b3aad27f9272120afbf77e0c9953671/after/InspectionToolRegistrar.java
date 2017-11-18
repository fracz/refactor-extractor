package com.intellij.codeInspection.ex;

import com.intellij.codeInspection.GlobalInspectionTool;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ui.InspectCodePanel;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ResourceUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author max
 */
public class InspectionToolRegistrar implements ApplicationComponent, JDOMExternalizable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ex.InspectionToolRegistrar");

  private final ArrayList<Class> myInspectionTools;
  private final ArrayList<Class> myLocalInspectionTools;
  private final ArrayList<Class> myGlobalInspectionTools;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
  private static HashMap<String, ArrayList<String>> myWords2InspectionToolNameMap = null;

  private static final Pattern HTML_PATTERN = Pattern.compile("<[^<>]*>");

  public InspectionToolRegistrar(InspectionToolProvider[] providers) {
    myInspectionTools = new ArrayList<Class>();
    myLocalInspectionTools = new ArrayList<Class>();
    myGlobalInspectionTools = new ArrayList<Class>();
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      for (InspectionToolProvider provider : providers) {
        Class[] classes = provider.getInspectionClasses();
        for (Class aClass : classes) {
          if (LocalInspectionTool.class.isAssignableFrom(aClass)) {
            registerLocalInspection(aClass);
          } else if (GlobalInspectionTool.class.isAssignableFrom(aClass)){
            registerGlobalInspection(aClass);
          } else {
            registerInspectionTool(aClass);
          }
        }
      }
    }
  }

  public static InspectionToolRegistrar getInstance() {
    return ApplicationManager.getApplication().getComponent(InspectionToolRegistrar.class);
  }

  public String getComponentName() {
    return "InspectionToolRegistrar";
  }

  public void readExternal(Element element) throws InvalidDataException {
  }

  public void writeExternal(Element element) throws WriteExternalException {
  }

  public void disposeComponent() {
  }

  public void initComponent() {
  }

  private void registerLocalInspection(Class toolClass) {
    myLocalInspectionTools.add(toolClass);
  }

  private void registerGlobalInspection(final Class aClass) {
    myGlobalInspectionTools.add(aClass);
  }

  private void registerInspectionTool(Class toolClass) {
    if (myInspectionTools.contains(toolClass)) return;
    myInspectionTools.add(toolClass);
  }

  public InspectionTool[] createTools() {
    int ordinaryToolsSize = myInspectionTools.size();
    final int withLocal = ordinaryToolsSize + myLocalInspectionTools.size();
    InspectionTool[] tools = new InspectionTool[withLocal + myGlobalInspectionTools.size()];
    for (int i = 0; i < ordinaryToolsSize; i++) {
      tools[i] = instantiateTool(myInspectionTools.get(i));
    }
    for(int i = ordinaryToolsSize; i < withLocal; i++){
      tools[i] = new LocalInspectionToolWrapper((LocalInspectionTool)instantiateWrapper(myLocalInspectionTools.get(i - ordinaryToolsSize)));
    }
    for(int i = withLocal; i < tools.length; i++){
      tools[i] = new GlobalInspectionToolWrapper((GlobalInspectionTool)instantiateWrapper(myGlobalInspectionTools.get(i - withLocal)));
    }

    buildInspectionIndex(tools);

    return tools;
  }

  private static Object instantiateWrapper(Class toolClass) {
    try {
      Constructor constructor = toolClass.getDeclaredConstructor(ArrayUtil.EMPTY_CLASS_ARRAY);
      Object[] args = ArrayUtil.EMPTY_OBJECT_ARRAY;
      return constructor.newInstance(args);
    } catch (NoSuchMethodException e) {
      LOG.error(e);
    } catch (SecurityException e) {
      LOG.error(e);
    } catch (InstantiationException e) {
      LOG.error(e);
    } catch (IllegalAccessException e) {
      LOG.error(e);
    } catch (IllegalArgumentException e) {
      LOG.error(e);
    } catch (InvocationTargetException e) {
      LOG.error(e);
    }
    return null;
  }

  private static InspectionTool instantiateTool(Class toolClass) {
    try {
      Constructor constructor = toolClass.getDeclaredConstructor(ArrayUtil.EMPTY_CLASS_ARRAY);
      constructor.setAccessible(true);
      return (InspectionTool) constructor.newInstance(ArrayUtil.EMPTY_OBJECT_ARRAY);
    } catch (SecurityException e) {
      LOG.error(e);
    } catch (NoSuchMethodException e) {
      LOG.error(e);
    } catch (InstantiationException e) {
      LOG.error(e);
    } catch (IllegalAccessException e) {
      LOG.error(e);
    } catch (IllegalArgumentException e) {
      LOG.error(e);
    } catch (InvocationTargetException e) {
      LOG.error(e);
    }

    return null;
  }

  private synchronized static void buildInspectionIndex(final InspectionTool[] tools) {
    if (myWords2InspectionToolNameMap == null) {
      myWords2InspectionToolNameMap = new HashMap<String, ArrayList<String>>();
      new Thread(){
        public void run() {
          try {
            for (InspectionTool tool : tools) {
              processText(tool.getDisplayName().toLowerCase(), tool);
              final URL description = getDescriptionUrl(tool, tool.getDescriptionFileName());
              if (description != null) {
                @NonNls String descriptionText = readInputStream(description.openStream()).toLowerCase();
                if (descriptionText != null) {
                  descriptionText = HTML_PATTERN.matcher(descriptionText).replaceAll(" ");
                  processText(descriptionText, tool);
                }
              }
            }
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
      }.start();
    }
  }

  private static void processText(final @NonNls @NotNull String descriptionText, final InspectionTool tool) {
    final Set<String> words = SearchUtil.getProcessedWords(descriptionText);
    for (String word : words) {
      ArrayList<String> descriptors = myWords2InspectionToolNameMap.get(word);
      if (descriptors == null) {
        descriptors = new ArrayList<String>();
        myWords2InspectionToolNameMap.put(word, descriptors);
      }
      descriptors.add(tool.getShortName());
    }
  }

  public static boolean isIndexBuild(){
    return myWords2InspectionToolNameMap != null;
  }

  public static List<String> getFilteredToolNames(String filter){
    return myWords2InspectionToolNameMap.get(filter);
  }

  public static Set<String> getToolWords(){
    return myWords2InspectionToolNameMap.keySet();
  }

  public static URL getDescriptionUrl(InspectionTool tool, String descriptionFileName) {
    Class aClass;
    if (tool != null) {
      if (tool instanceof LocalInspectionToolWrapper) {
        aClass = ((LocalInspectionToolWrapper)tool).getTool().getClass();
      }
      else if (tool instanceof GlobalInspectionToolWrapper) {
        aClass = ((GlobalInspectionToolWrapper)tool).getTool().getClass();
      }
      else {
        aClass = tool.getClass();
      }
    }
    else {
      aClass = InspectCodePanel.class;
    }
    return ResourceUtil.getResource(aClass, "/inspectionDescriptions", descriptionFileName);
  }

  private static String readInputStream(InputStream in) {
    try {
      StringBuffer str = new StringBuffer();
      int c = in.read();
      while (c != -1) {
        str.append((char)c);
        c = in.read();
      }
      return str.toString();
    }
    catch (IOException e) {
      LOG.error(e);
    }
    return null;
  }
}