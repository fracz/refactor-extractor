/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.openapi.extensions.impl;

import com.intellij.openapi.extensions.LoadingOrder;
import com.intellij.openapi.extensions.PluginAware;
import com.intellij.openapi.extensions.ReaderConfigurator;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.core.DefaultClassMapper;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import org.jdom.Element;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * @author Alexander Kireyev
 */
public class ExtensionComponentAdapter extends ConstructorInjectionComponentAdapter implements LoadingOrder.Orderable {
  private Object myComponentInstance;
  private Element myExtensionElement;
  private PicoContainer myContainer;
  private PluginDescriptor myPluginDescriptor;

  public ExtensionComponentAdapter(Class implementationClass, Element extensionElement, PicoContainer container, PluginDescriptor pluginDescriptor) {
    super(new Object(), implementationClass);
    myExtensionElement = extensionElement;
    myContainer = container;
    myPluginDescriptor = pluginDescriptor;
  }

  public Object getComponentInstance(final PicoContainer container) throws PicoInitializationException, PicoIntrospectionException, AssignabilityRegistrationException, NotConcreteRegistrationException {
    assert myContainer == container;

    if (myComponentInstance == null) {
      if (!Element.class.equals(getComponentImplementation())) {
        final CompositeClassLoader classLoader = new CompositeClassLoader();
        if (myPluginDescriptor.getPluginClassLoader() != null) {
          classLoader.add(myPluginDescriptor.getPluginClassLoader());
        }
        XStream xStream = new XStream(new PropertyReflectionProvider(), new DefaultClassMapper(classLoader), new XppDriver());
        xStream.registerConverter(new ElementConverter());
        Object componentInstance = super.getComponentInstance(container);
        if (componentInstance instanceof ReaderConfigurator) {
          ReaderConfigurator readerConfigurator = (ReaderConfigurator) componentInstance;
          readerConfigurator.configureReader(xStream);
        }
        xStream.alias(myExtensionElement.getName(), componentInstance.getClass());
        myComponentInstance = xStream.unmarshal(new JDomExtensionsReader(myExtensionElement), componentInstance);
      }
      else {
        myComponentInstance = myExtensionElement;
      }
      if (myComponentInstance instanceof PluginAware) {
        PluginAware pluginAware = (PluginAware) myComponentInstance;
        pluginAware.setPluginDescriptor(myPluginDescriptor);
      }
    }

    return myComponentInstance;
  }

  public Object getExtension() {
    return getComponentInstance(myContainer);
  }

  public LoadingOrder getOrder() {
    String orderAttr = myExtensionElement.getAttributeValue("order");
    return LoadingOrder.readOrder(orderAttr);
  }

  public String getOrderId() {
    return myExtensionElement.getAttributeValue("id");
  }

  public Element getExtensionElement() {
    return myExtensionElement;
  }

  public Element getDescribingElement() {
    return getExtensionElement();
  }

  public String getPluginName() {
    return myPluginDescriptor.getPluginName();
  }
}