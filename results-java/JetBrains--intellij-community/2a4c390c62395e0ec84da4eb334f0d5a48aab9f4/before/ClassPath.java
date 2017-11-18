package com.intellij.util.lang;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import sun.misc.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

class ClassPath {
  private final Stack<URL> myUrls = new Stack<URL>();
  private ArrayList<Loader> myLoaders = new ArrayList<Loader>();
  private HashMap<URL,Loader> myLoadersMap = new HashMap<URL, Loader>();
  @NonNls private static final String FILE_PROTOCOL = "file";
  private static boolean myDebugTime = false;
  private boolean myCanLockJars;
  private final boolean myCanUseCache;
  private static final long NS_THRESHOLD = 10000000L;

  public ClassPath(URL[] urls, boolean canLockJars, boolean canUseCache) {
    myCanLockJars = canLockJars;
    myCanUseCache = canUseCache;
    push(urls);
  }

  void addURL(URL url) {
    push(new URL[]{url});
  }

  @Nullable
  public Resource getResource(String s, boolean flag) {
    final long started = myDebugTime ? System.nanoTime():0;

    try {
      Loader loader;

      for (int i = 0; (loader = getLoader(i)) != null; i++) {
        Resource resource = loader.getResource(s, flag);
        if (resource != null) {
          return resource;
        }
      }

      return null;
    }
    finally {
      long doneFor = myDebugTime ? System.nanoTime() - started:0;
      if (doneFor > NS_THRESHOLD) {
        System.out.println((doneFor/1000000) + " ms for getResource:"+s+", flag:"+flag);
      }
    }
  }

  public Enumeration<URL> getResources(final String name, final boolean check) {
    return new MyEnumeration(name, check);
  }

  @Nullable
  private synchronized Loader getLoader(int i) {
    while (myLoaders.size() < i + 1) {
      URL url;
      synchronized (myUrls) {
        if (myUrls.empty()) return null;
        url = myUrls.pop();
      }

      if (myLoadersMap.containsKey(url)) continue;

      Loader loader;
      try {
        loader = getLoader(url);
        if (loader == null) continue;
      }
      catch (IOException ioexception) {
        continue;
      }

      myLoaders.add(loader);
      myLoadersMap.put(url, loader);
    }

    return myLoaders.get(i);
  }

  @Nullable
  private Loader getLoader(final URL url) throws IOException {
    String s = url.getFile();

    if (s != null && StringUtil.endsWithChar(s, '/')) {
      if (FILE_PROTOCOL.equals(url.getProtocol())) return new FileLoader(url, myCanUseCache);
    }
    else {
      return new JarLoader(url, myCanLockJars, myCanUseCache);
    }

    //add custom loaders here
    return null;
  }

  private void push(URL[] urls) {
    synchronized (myUrls) {
      for (int i = urls.length - 1; i >= 0; i--) myUrls.push(urls[i]);

    }
  }

  private class MyEnumeration implements Enumeration<URL> {
    private int myIndex = 0;
    private Resource myRes = null;
    private final String myName;
    private final boolean myCheck;

    public MyEnumeration(String name, boolean check) {
      myName = name;
      myCheck = check;
    }

    private boolean next() {
      if (myRes != null) return true;

      Loader loader;
      while ((loader = getLoader(myIndex++)) != null) {
        myRes = loader.getResource(myName, myCheck);
        if (myRes != null) return true;
      }

      return false;
    }

    public boolean hasMoreElements() {
      return next();
    }

    public URL nextElement() {
      if (!next()) {
        throw new NoSuchElementException();
      }
      else {
        Resource resource = myRes;
        myRes = null;
        return resource.getURL();
      }
    }
  }
}