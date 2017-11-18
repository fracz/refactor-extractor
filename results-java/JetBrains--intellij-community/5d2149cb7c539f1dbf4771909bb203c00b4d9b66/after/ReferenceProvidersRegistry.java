package com.intellij.psi.impl.source.resolve.reference;

import com.intellij.ant.impl.dom.impl.RegisterInPsi;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiPlainTextFile;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.filters.position.TokenTypeFilter;
import com.intellij.psi.filters.position.SuperParentFilter;
import com.intellij.psi.impl.source.resolve.ResolveUtil;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.JspAttributeValueManipulator;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.PlainFileManipulator;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.XmlAttributeValueManipulator;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JSPActionReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassListReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JspxIncludePathReferenceProvider;
import com.intellij.psi.jsp.JspAction;
import com.intellij.psi.jsp.JspAttributeValue;
import com.intellij.psi.jsp.JspDirective;
import com.intellij.psi.jsp.JspToken;
import com.intellij.psi.xml.*;
import com.intellij.xml.util.XmlUtil;
import com.intellij.xml.util.HtmlUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import electric.util.directory.filters.NameFilter;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 27.03.2003
 * Time: 17:13:45
 * To change this template use Options | File Templates.
 */
public class ReferenceProvidersRegistry implements ProjectComponent {
  private final List<Class> myTempScopes = new ArrayList<Class>();
  private final List<ProviderBinding> myBindings = new ArrayList<ProviderBinding>();
  private final List<Object[]> myManipulators = new ArrayList<Object[]>();

  public static final ReferenceProvidersRegistry getInstance(Project project) {
    return project.getComponent(ReferenceProvidersRegistry.class);
  }

  private ReferenceProvidersRegistry() {
    // Temp scopes declarations
    myTempScopes.add(JspToken.class);
    myTempScopes.add(PsiIdentifier.class);

    // Manipulators mapping
    registerManipulator(JspAttributeValue.class, new JspAttributeValueManipulator());
    registerManipulator(XmlAttributeValue.class, new XmlAttributeValueManipulator());
    registerManipulator(PsiPlainTextFile.class, new PlainFileManipulator());
    // Binding declarations
    registerReferenceProvider(
      new ScopeFilter(
        new AndFilter(
          new ParentElementFilter(
            new TextFilter(new String[]{"class", "type"})
          ),
          new ParentElementFilter(
            new TextFilter("useBean"), 2
          )
        )
      ),
      XmlAttributeValue.class,
      new JavaClassReferenceProvider()
    );
    RegisterInPsi.referenceProviders(this);
    registerReferenceProvider(
      new ScopeFilter(
        new ParentElementFilter(
          new AndFilter(
            new TextFilter("extends"),
            new ParentElementFilter(
              new AndFilter(
                new ClassFilter(JspDirective.class),
                new TextFilter("page")
              )
            )
          )
        )
      ),
      JspAttributeValue.class,
      new JavaClassReferenceProvider()
    );
    registerReferenceProvider(
      new ScopeFilter(
        new AndFilter(
          new SuperParentFilter(
            new NamespaceFilter(XmlUtil.JSP_NAMESPACE)
          ),
          new ParentElementFilter(
            new AndFilter(
              new ClassFilter(XmlTag.class),
              new TextFilter("jsp:directive.include")

            ), 2
          )
        )
      ),
      XmlAttributeValue.class,
      new JspxIncludePathReferenceProvider()
    );
    //registerReferenceProvider(new ScopeFilter(new ParentElementFilter(new AndFilter(new TextFilter("target"),
    //                                                                                new ParentElementFilter(new AndFilter(
    //                                                                                  new NamespaceFilter(XmlUtil.ANT_URI),
    //                                                                                  new TextFilter("antcall")))))),
    //                          XmlAttributeValue.class, new AntTargetReferenceProvider());
    registerReferenceProvider(JspAction.class, new JSPActionReferenceProvider());
    registerReferenceProvider(new NotFilter(new ParentElementFilter(new NamespaceFilter(XmlUtil.ANT_URI), 2)),
                              XmlAttributeValue.class, new JavaClassListReferenceProvider());
    registerReferenceProvider(new TokenTypeFilter(XmlTokenType.XML_DATA_CHARACTERS), XmlToken.class,
                              new JavaClassListReferenceProvider());

    //registerReferenceProvider(PsiPlainTextFile.class, new JavaClassListReferenceProvider());

    HtmlUtil.HtmlReferenceProvider provider = new HtmlUtil.HtmlReferenceProvider();
    registerReferenceProvider(provider.getFilter(), XmlAttributeValue.class, provider);
  }

  public void registerReferenceProvider(ElementFilter elementFilter, Class scope, PsiReferenceProvider provider) {
    final ProviderBinding binding = new ProviderBinding(elementFilter, scope);
    binding.registerProvider(provider);
    myBindings.add(binding);
  }

  public void registerReferenceProvider(Class scope, PsiReferenceProvider provider) {
    final ProviderBinding binding = new ProviderBinding(scope);
    binding.registerProvider(provider);
    myBindings.add(binding);
  }

  public PsiReferenceProvider[] getProvidersByElement(PsiElement element) {
    final List<PsiReferenceProvider> ret = new ArrayList<PsiReferenceProvider>();
    PsiElement current;
    do {
      current = element;
      final Iterator<ProviderBinding> iter = myBindings.iterator();

      while (iter.hasNext()) {
        final ProviderBinding binding = iter.next();
        if (binding.isAcceptable(current)) {
          ret.addAll(Arrays.asList(binding.getProviders()));
        }
      }
      element = ResolveUtil.getContext(element);
    }
    while (!isScopeFinal(current.getClass()));

    return ret.toArray(new PsiReferenceProvider[ret.size()]);
  }

  public ElementManipulator getManipulator(PsiElement element) {
    if(element == null) return null;
    final Iterator<Object[]> iter = myManipulators.iterator();

    while (iter.hasNext()) {
      final Object[] pair = iter.next();
      if (((Class)pair[0]).isAssignableFrom(element.getClass())) {
        return (ElementManipulator)pair[1];
      }
    }

    return null;
  }

  public void registerManipulator(Class elementClass, ElementManipulator manipulator) {
    myManipulators.add(new Object[]{elementClass, manipulator});
  }

  private boolean isScopeFinal(Class scopeClass) {
    final Iterator iter = myTempScopes.iterator();

    while (iter.hasNext()) {
      final Class currentClass = (Class)iter.next();
      if (currentClass.isAssignableFrom(scopeClass)) {
        return false;
      }
    }
    return true;
  }

  public void projectOpened() {}

  public void projectClosed() {}

  public String getComponentName() {
    return "Reference providers registry";
  }

  public void initComponent() {}

  public void disposeComponent() {}
}