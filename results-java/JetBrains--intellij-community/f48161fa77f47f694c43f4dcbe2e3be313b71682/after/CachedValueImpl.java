/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Jun 6, 2002
 * Time: 5:41:42 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.psi.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.TimedReference;
import gnu.trove.TLongArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachedValueImpl<T> implements CachedValue<T> {
  private static final Object NULL = new Object();
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.CachedValueImpl");

  private final PsiManager myManager;
  private final CachedValueProvider<T> myProvider;
  private final boolean myTrackValue;

  private final TimedReference<Data<T>> myData = new TimedReference<Data<T>>(null);


  private long myLastPsiTimeStamp = -1;
  private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
  private Lock r = rw.readLock();
  private Lock w = rw.writeLock();

  public CachedValueImpl(PsiManager manager, CachedValueProvider<T> provider, boolean trackValue) {
    myManager = manager;
    myProvider = provider;
    myTrackValue = trackValue;
  }

  private static class Data<T> implements Disposable {
    private final T myValue;
    private final Object[] myDependencies;
    private final long[] myTimeStamps;

    public Data(final T value, final Object[] dependencies, final long[] timeStamps) {
      myValue = value;
      myDependencies = dependencies;
      myTimeStamps = timeStamps;
    }

    public void dispose() {
      if (myValue instanceof Disposable) {
        Disposer.dispose((Disposable)myValue);
      }
    }
  }

  @Nullable
  public T getValue() {
    r.lock();

    try {
      T value = getUpToDateOrNull();
      if (value != null) {
        return value == NULL ? null : value;
      }

      r.unlock();
      w.lock();

      try {
        value = getUpToDateOrNull();
        if (value != null) {
          return value == NULL ? null : value;
        }


        CachedValueProvider.Result<T> result = myProvider.compute();
        value = result == null ? null : result.getValue();

        setValue(value, result);

        return value;
      }
      finally {
        w.unlock();
        r.lock();
      }
    }
    finally {
      r.unlock();
    }
  }

  private void setValue(final T value, final CachedValueProvider.Result<T> result) {
    myData.set(computeData(value == null ? (T) NULL : value, result == null ? null : result.getDependencyItems()));
  }

  public boolean hasUpToDateValue() {
    r.lock();

    try {
      return getUpToDateOrNull() != null;
    }
    finally {
      r.unlock();
    }
  }

  @Nullable
  private T getUpToDateOrNull() {
    final Data<T> data = myData.get();

    if (data != null) {
      T value = data.myValue;
      if (isUpToDate(data)) {
        return value;
      }
      if (value instanceof Disposable) {
        Disposer.dispose((Disposable)value);
      }
    }
    return null;
  }

  private boolean isUpToDate(@NotNull Data data) {
    if (data.myTimeStamps == null) return true;
    if (myManager.isDisposed()) return false;

    for (int i = 0; i < data.myDependencies.length; i++) {
      Object dependency = data.myDependencies[i];
      if (dependency == null) continue;
      if (isDependencyOutOfDate(dependency, data.myTimeStamps[i])) return false;
    }

    return true;
  }

  private boolean isDependencyOutOfDate(Object dependency, long oldTimeStamp) {
    if (dependency instanceof PsiElement &&
        myLastPsiTimeStamp == myManager.getModificationTracker().getModificationCount()) {
      return false;
    }
    final long timeStamp = getTimeStamp(dependency);
    return timeStamp < 0 || timeStamp != oldTimeStamp;
  }

  private Data<T> computeData(T value, Object[] dependencies) {
    if (dependencies == null) {
      return new Data<T>(value, null, null);
    }

    TLongArrayList timeStamps = new TLongArrayList();
    List<Object> deps = new ArrayList<Object>();
    collectDependencies(timeStamps, deps, dependencies);
    if (myTrackValue) {
      collectDependencies(timeStamps, deps, new Object[]{value});
    }

    myLastPsiTimeStamp = myManager.getModificationTracker().getModificationCount();

    return  new Data<T>(value, deps.toArray(new Object[deps.size()]), timeStamps.toNativeArray());
  }

  private void collectDependencies(TLongArrayList timeStamps, List<Object> resultingDeps, Object[] dependencies) {
    for (Object dependency : dependencies) {
      if (dependency == null || dependency == NULL) continue;
      if (dependency instanceof Object[]) {
        collectDependencies(timeStamps, resultingDeps, (Object[])dependency);
      }
      else {
        resultingDeps.add(dependency);
        timeStamps.add(getTimeStamp(dependency));
      }
    }
  }

  private long getTimeStamp(Object dependency) {
    if (dependency instanceof Reference){
      final Object original = ((Reference)dependency).get();
      if(original == null) return -1;
      return getTimeStamp(original);
    }

    if (dependency instanceof Ref) {
      final Object original = ((Ref)dependency).get();
      if(original == null) return -1;
      return getTimeStamp(original);
    }

    if (dependency instanceof ModificationTracker) {
      return ((ModificationTracker)dependency).getModificationCount();
    }

    if (dependency instanceof PsiDirectory) {
      return myManager.getModificationTracker().getOutOfCodeBlockModificationCount();
    }

    if (dependency instanceof PsiElement) {
      PsiElement element = (PsiElement)dependency;
      if (!element.isValid()) return -1;
      PsiFile containingFile = element.getContainingFile();
      if (containingFile == null) return -1;
      return containingFile.getModificationStamp();
    }

    if (dependency == PsiModificationTracker.MODIFICATION_COUNT) {
      return myManager.getModificationTracker().getModificationCount();
    }
    else if (dependency == PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT) {
      return myManager.getModificationTracker().getOutOfCodeBlockModificationCount();
    } else {
      LOG.error("Wrong dependency type: " + dependency.getClass());
      return -1;
    }
  }

  public CachedValueProvider<T> getValueProvider() {
    return myProvider;
  }

  public T setValue(final CachedValueProvider.Result<T> result) {
    w.lock();

    try {
      T value = result.getValue();
      setValue(value, result);
      return value;
    }
    finally {
      w.unlock();
    }
  }
}