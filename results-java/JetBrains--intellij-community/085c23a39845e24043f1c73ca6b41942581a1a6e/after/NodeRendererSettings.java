package com.intellij.debugger.settings;

import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.ui.tree.ValueDescriptor;
import com.intellij.debugger.ui.tree.render.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.*;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.InternalIterator;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * User: lex
 * Date: Sep 18, 2003
 * Time: 8:00:25 PM
 */
public class NodeRendererSettings implements ApplicationComponent, NamedJDOMExternalizable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.settings.NodeRendererSettings");
  private static final String REFERENCE_RENDERER = "Reference renderer";
  public static final String RENDERER_TAG = "Renderer";
  private static final String RENDERER_ID = "ID";

  private final EventDispatcher<NodeRendererSettingsListener> myDispatcher = EventDispatcher.create(NodeRendererSettingsListener.class);
  private List<NodeRenderer> myPluginRenderers = new ArrayList<NodeRenderer>();
  private RendererConfiguration myCustomRenderers = new RendererConfiguration(this);

  // base renderers
  private final PrimitiveRenderer myPrimitiveRenderer = new PrimitiveRenderer();
  private final ArrayRenderer myArrayRenderer = new ArrayRenderer();
  private final ClassRenderer myClassRenderer = new ClassRenderer();
  private final HexRenderer myHexRenderer = new HexRenderer();
  private final ToStringRenderer myToStringRenderer = new ToStringRenderer();
  // alternate collections
  private final NodeRenderer[] myAlternateCollectionRenderers = new NodeRenderer[]{
    createCompoundReferenceRenderer(
      "Map", "java.util.Map",
      createLabelRenderer(" size = ", "size()", null),
      createExpressionChildrenRenderer("entrySet().toArray()", "!isEmpty()")
    ),
    createCompoundReferenceRenderer(
      "Map.Entry", "java.util.Map$Entry",
      createLabelRenderer(null, "\" \" + getKey() + \" -> \" + getValue()", null),
      createEnumerationChildrenRenderer(new String[][]{{"key", "getKey()"}, {"value", "getValue()"}})
    ),
    createCompoundReferenceRenderer(
      "Collection", "java.util.Collection",
      createLabelRenderer(" size = ", "size()", null),
      createExpressionChildrenRenderer("toArray()", "!isEmpty()")
    )
  };
  private static final String HEX_VIEW_ENABLED = "HEX_VIEW_ENABLED";
  private static final String ALTERNATIVE_COLLECTION_VIEW_ENABLED = "ALTERNATIVE_COLLECTION_VIEW_ENABLED";
  private static final String CUSTOM_RENDERERS_TAG_NAME = "CustomRenderers";

  public NodeRendererSettings() {
    // default configuration
    myHexRenderer.setEnabled(false);
    myToStringRenderer.setEnabled(true);
    setAlternateCollectionViewsEnabled(true);
  }

  public static NodeRendererSettings getInstance() {
    return ApplicationManager.getApplication().getComponent(NodeRendererSettings.class);
  }

  public void addPluginRenderer(NodeRenderer renderer) {
    myPluginRenderers.add(renderer);
  }

  public void removePluginRenderer(NodeRenderer renderer) {
    myPluginRenderers.remove(renderer);
  }

  public void setAlternateCollectionViewsEnabled(boolean enabled) {
    for (NodeRenderer myAlternateCollectionRenderer : myAlternateCollectionRenderers) {
      myAlternateCollectionRenderer.setEnabled(enabled);
    }
  }

  public boolean areAlternateCollectionViewsEnabled() {
    return myAlternateCollectionRenderers[0].isEnabled();
  }

  public String getComponentName() {
    return "NodeRendererSettings";
  }

  public void initComponent() { }

  public void disposeComponent() {
  }

  public String getExternalFileName() {
    return "debugger.renderers";
  }

  public boolean equals(Object o) {
    if(!(o instanceof NodeRendererSettings)) return false;

    return DebuggerUtilsEx.externalizableEqual(this, (NodeRendererSettings)o);
  }

  public void addListener(NodeRendererSettingsListener listener) {
    myDispatcher.addListener(listener);
  }

  public void removeListener(NodeRendererSettingsListener listener) {
    myDispatcher.removeListener(listener);
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    JDOMExternalizerUtil.writeField(element, HEX_VIEW_ENABLED, myHexRenderer.isEnabled()? "true" : "false");
    JDOMExternalizerUtil.writeField(element, ALTERNATIVE_COLLECTION_VIEW_ENABLED, areAlternateCollectionViewsEnabled()? "true" : "false");
    element.addContent(writeRenderer(myArrayRenderer));
    element.addContent(writeRenderer(myToStringRenderer));
    element.addContent(writeRenderer(myClassRenderer));
    if (myCustomRenderers.getRendererCount() > 0) {
      final Element custom = new Element(CUSTOM_RENDERERS_TAG_NAME);
      element.addContent(custom);
      myCustomRenderers.writeExternal(custom);
    }
  }

  public void readExternal(final Element root) throws InvalidDataException {
    final String hexEnabled = JDOMExternalizerUtil.readField(root, HEX_VIEW_ENABLED);
    if (hexEnabled != null) {
      myHexRenderer.setEnabled("true".equalsIgnoreCase(hexEnabled));
    }

    final String alternativeEnabled = JDOMExternalizerUtil.readField(root, ALTERNATIVE_COLLECTION_VIEW_ENABLED);
    if (alternativeEnabled != null) {
      setAlternateCollectionViewsEnabled("true".equalsIgnoreCase(alternativeEnabled));
    }

    final List rendererElements = root.getChildren(RENDERER_TAG);
    for (final Object rendererElement : rendererElements) {
      final Element elem = (Element)rendererElement;
      final String id = elem.getAttributeValue(RENDERER_ID);
      if (id == null) {
        continue;
      }
      if (ArrayRenderer.UNIQUE_ID.equals(id)) {
        myArrayRenderer.readExternal(elem);
      }
      else if (ToStringRenderer.UNIQUE_ID.equals(id)) {
        myToStringRenderer.readExternal(elem);
      }
      else if (ClassRenderer.UNIQUE_ID.equals(id)) {
        myClassRenderer.readExternal(elem);
      }
    }
    final Element custom = root.getChild(CUSTOM_RENDERERS_TAG_NAME);
    if (custom != null) {
      myCustomRenderers.readExternal(custom);
    }

    myDispatcher.getMulticaster().renderersChanged();
  }

  public RendererConfiguration getCustomRenderers() {
    return myCustomRenderers;
  }

  public void setCustomRenderers(final RendererConfiguration customRenderers) {
    LOG.assertTrue(customRenderers != null);
    RendererConfiguration oldConfig = myCustomRenderers;
    myCustomRenderers = customRenderers;
    if (oldConfig == null || !oldConfig.equals(customRenderers)) {
      fireRenderersChanged();
    }
  }

  public List<NodeRenderer> getPluginRenderers() {
    return new ArrayList<NodeRenderer>(myPluginRenderers);
  }

  public PrimitiveRenderer getPrimitiveRenderer() {
    return myPrimitiveRenderer;
  }

  public ArrayRenderer getArrayRenderer() {
    return myArrayRenderer;
  }

  public ClassRenderer getClassRenderer() {
    return myClassRenderer;
  }

  public HexRenderer getHexRenderer() {
    return myHexRenderer;
  }

  public ToStringRenderer getToStringRenderer() {
    return myToStringRenderer;
  }

  public NodeRenderer[] getAlternateCollectionRenderers() {
    return myAlternateCollectionRenderers;
  }

  public void fireRenderersChanged() {
    myDispatcher.getMulticaster().renderersChanged();
  }

  public List<NodeRenderer> getAllRenderers() {
    // the order is important as the renderers are applied according to it
    final List<NodeRenderer> allRenderers = new ArrayList<NodeRenderer>();
    allRenderers.add(myHexRenderer);
    allRenderers.add(myPrimitiveRenderer);
    allRenderers.addAll(myPluginRenderers);
    myCustomRenderers.iterateRenderers(new InternalIterator<NodeRenderer>() {
      public boolean visit(final NodeRenderer renderer) {
        allRenderers.add(renderer);
        return true;
      }
    });
    for (NodeRenderer myAlternateCollectionRenderer : myAlternateCollectionRenderers) {
      allRenderers.add(myAlternateCollectionRenderer);
    }
    allRenderers.add(myToStringRenderer);
    allRenderers.add(myArrayRenderer);
    allRenderers.add(myClassRenderer);
    return allRenderers;
  }

  public boolean isBase(final Renderer renderer) {
    return renderer == myPrimitiveRenderer || renderer == myArrayRenderer || renderer == myClassRenderer;
  }

  public Renderer readRenderer(Element root) throws InvalidDataException {
    if (root == null) {
      return null;
    }

    if (!RENDERER_TAG.equals(root.getName())) {
      throw new InvalidDataException("Cannot read renderer - tag name is not '" + RENDERER_TAG + "'");
    }

    final String rendererId = root.getAttributeValue(RENDERER_ID);
    if(rendererId == null) {
      throw new InvalidDataException("unknown renderer ID: " + rendererId);
    }

    final Renderer renderer = createRenderer(rendererId);
    if(renderer == null) {
      throw new InvalidDataException("unknown renderer ID: " + rendererId);
    }

    renderer.readExternal(root);

    return renderer;
  }

  public Element writeRenderer(Renderer renderer) throws WriteExternalException {
    Element root = new Element(RENDERER_TAG);
    if(renderer != null) {
      root.setAttribute(RENDERER_ID  , renderer.getUniqueId());
      renderer.writeExternal(root);
    }
    return root;
  }

  public Renderer createRenderer(final String rendererId) {
    if (ClassRenderer.UNIQUE_ID.equals(rendererId)) {
      return myClassRenderer;
    }
    else if (ArrayRenderer.UNIQUE_ID.equals(rendererId)) {
      return myArrayRenderer;
    }
    else if (PrimitiveRenderer.UNIQUE_ID.equals(rendererId)) {
      return myPrimitiveRenderer;
    }
    else if(HexRenderer.UNIQUE_ID.equals(rendererId)) {
      return myHexRenderer;
    }
    else if(rendererId.equals(ExpressionChildrenRenderer.UNIQUE_ID)) {
      return new ExpressionChildrenRenderer();
    }
    else if(rendererId.equals(LabelRenderer.UNIQUE_ID)) {
      return new LabelRenderer();
    }
    else if(rendererId.equals(EnumerationChildrenRenderer.UNIQUE_ID)) {
      return new EnumerationChildrenRenderer();
    }
    else if(rendererId.equals(ToStringRenderer.UNIQUE_ID)) {
      return myToStringRenderer;
    }
    else if(rendererId.equals(CompoundNodeRenderer.UNIQUE_ID) || rendererId.equals(REFERENCE_RENDERER)) {
      return createCompoundReferenceRenderer("unnamed", "java.lang.Object", null, null);
    }
    return null;
  }

  private CompoundReferenceRenderer createCompoundReferenceRenderer(
    final String rendererName, final String className, final LabelRenderer labelRenderer, final ChildrenRenderer childrenRenderer
    ) {
    CompoundReferenceRenderer renderer = new CompoundReferenceRenderer(this, rendererName, labelRenderer, childrenRenderer);
    renderer.setClassName(className);
    return renderer;
  }

  private ExpressionChildrenRenderer createExpressionChildrenRenderer(String expressionText, String childrenExpandableText) {
    final ExpressionChildrenRenderer childrenRenderer = new ExpressionChildrenRenderer();
    childrenRenderer.setChildrenExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, expressionText));
    if (childrenExpandableText != null) {
      childrenRenderer.setChildrenExpandable(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, childrenExpandableText));
    }
    return childrenRenderer;
  }

  private EnumerationChildrenRenderer createEnumerationChildrenRenderer(String[][] expressions) {
    final EnumerationChildrenRenderer childrenRenderer = new EnumerationChildrenRenderer();
    if (expressions != null && expressions.length > 0) {
      final ArrayList<Pair<String, TextWithImports>> childrenList = new ArrayList<Pair<String, TextWithImports>>(expressions.length);
      for (final String[] expression : expressions) {
        childrenList.add(new Pair<String, TextWithImports>(expression[0], new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, expression[1])));
      }
      childrenRenderer.setChildren(childrenList);
    }
    return childrenRenderer;
  }

  private LabelRenderer createLabelRenderer(final String prefix, final String expressionText, final String postfix) {
    final LabelRenderer labelRenderer = new LabelRenderer() {
      public String calcLabel(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener labelListener) throws EvaluateException {
        final String evaluated = super.calcLabel(descriptor, evaluationContext, labelListener);
        if (prefix == null && postfix == null) {
          return evaluated;
        }
        if (prefix != null && postfix != null) {
          return prefix + evaluated + postfix;
        }
        if (prefix != null) {
          return prefix + evaluated;
        }
        return evaluated + postfix;
      }
    };
    labelRenderer.setLabelExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, expressionText));
    return labelRenderer;
  }

}