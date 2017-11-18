/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.intellij.pom.core.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.pom.PomModel;
import com.intellij.pom.PomModelAspect;
import com.intellij.pom.PomProject;
import com.intellij.pom.PomTransaction;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.pom.event.PomModelListener;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLock;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiToDocumentSynchronizer;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

import java.util.*;

public class PomModelImpl extends UserDataHolderBase implements PomModel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.pom.core.impl.PomModelImpl");
  private final PomProject myPomProject;
  private Map<Class<? extends PomModelAspect>, PomModelAspect> myAspects = new HashMap<Class<? extends PomModelAspect>, PomModelAspect>();
  private Map<PomModelAspect, Set<PomModelAspect>> myAspectDependencies = new HashMap<PomModelAspect, Set<PomModelAspect>>();
  private Map<PomModelAspect, List<PomModelAspect>> myIncidence = new HashMap<PomModelAspect, List<PomModelAspect>>();
  private Map<PomModelAspect, List<PomModelAspect>> myInvertedIncidence = new HashMap<PomModelAspect, List<PomModelAspect>>();
  private List<PomModelListener> myListeners = new ArrayList<PomModelListener>();

  public PomModelImpl(Project project) {
    myPomProject = new PomProjectImpl(this, project);
  }

  public PomProject getRoot() {
    return myPomProject;
  }

  public <T extends PomModelAspect> T getModelAspect(Class<T> aClass) {
    return (T)myAspects.get(aClass);
  }

  public void registerAspect(Class<? extends PomModelAspect> aClass, PomModelAspect aspect, Set<PomModelAspect> dependencies) {
    myAspectDependencies.put(aspect, dependencies);
    myAspects.put(aClass, aspect);
    final Iterator<PomModelAspect> iterator = dependencies.iterator();
    final List<PomModelAspect> deps = new ArrayList<PomModelAspect>();
    // todo: reorder dependencies
    while (iterator.hasNext()) {
      final PomModelAspect depend = iterator.next();
      deps.addAll(getAllDependencies(depend));
    }
    deps.add(aspect); // add self to block same aspect transactions from event processing and update
    final Iterator<PomModelAspect> depsIterator = deps.iterator();
    while (depsIterator.hasNext()) {
      final PomModelAspect pomModelAspect = depsIterator.next();
      final List<PomModelAspect> pomModelAspects = myInvertedIncidence.get(pomModelAspect);
      if (pomModelAspects != null) {
        pomModelAspects.add(aspect);
      }
      else {
        myInvertedIncidence.put(pomModelAspect, new ArrayList<PomModelAspect>(Collections.singletonList(aspect)));
      }
    }
    myIncidence.put(aspect, deps);
  }

  //private final Pair<PomModelAspect, PomModelAspect> myHolderPair = new Pair<PomModelAspect, PomModelAspect>(null, null);
  public List<PomModelAspect> getAllDependencies(PomModelAspect aspect){
    List<PomModelAspect> pomModelAspects = myIncidence.get(aspect);
    return pomModelAspects != null ? pomModelAspects : Collections.EMPTY_LIST;
  }

  public List<PomModelAspect> getAllDependants(PomModelAspect aspect){
    List<PomModelAspect> pomModelAspects = myInvertedIncidence.get(aspect);
    return pomModelAspects != null ? pomModelAspects : Collections.EMPTY_LIST;
  }

  public void addModelListener(PomModelListener listener) {
    synchronized(myListeners){
      myListeners.add(listener);
      myListenersArray = null;
    }
  }

  public void removeModelListener(PomModelListener listener) {
    synchronized(myListeners){
      myListeners.remove(listener);
      myListenersArray = null;
    }
  }

  private final Stack<Pair<PomModelAspect, PomTransaction>> myBlockedAspects = new Stack<Pair<PomModelAspect, PomTransaction>>();

  public synchronized void runTransaction(PomTransaction transaction,
                                          PomModelAspect aspect) throws IncorrectOperationException{
    synchronized(PsiLock.LOCK){
      startTransaction(transaction);
      try{

        final PomModelEvent event;
        myBlockedAspects.push(new Pair<PomModelAspect, PomTransaction>(aspect, transaction));

        try{
          transaction.run();
          event = transaction.getAccumulatedEvent();
        }
        catch(IncorrectOperationException ioe){
          return;
        }
        finally{
          myBlockedAspects.pop();
        }
        final Pair<PomModelAspect,PomTransaction> block = getBlockingTransaction(aspect, transaction);
        if(block != null){
          final PomModelEvent currentEvent = block.getSecond().getAccumulatedEvent();
          currentEvent.merge(event);
          return;
        }

        { // update
          final Set<PomModelAspect> changedAspects = event.getChangedAspects();
          final Set<PomModelAspect> dependants = new HashSet<PomModelAspect>();
          final Iterator<PomModelAspect> changedIterator = changedAspects.iterator();
          while (changedIterator.hasNext()) {
            final PomModelAspect pomModelAspect = changedIterator.next();
            dependants.addAll(getAllDependants(pomModelAspect));
          }
          final Iterator<PomModelAspect> depsIter = dependants.iterator();
          while (depsIter.hasNext()) {
            final PomModelAspect modelAspect = depsIter.next();
            if(myBlockedAspects.contains(modelAspect) || changedAspects.contains(modelAspect)) continue;
            modelAspect.update(event);
          }
        }
        {
          final PomModelListener[] listeners = getListeners();
          for (int i = 0; i < listeners.length; i++) {
            final PomModelListener listener = listeners[i];
            final Set<PomModelAspect> changedAspects = event.getChangedAspects();
            for (Iterator<PomModelAspect> j = changedAspects.iterator(); j.hasNext();) {
              PomModelAspect modelAspect = j.next();
              if (listener.isAspectChangeInteresting(modelAspect)) {
                listener.modelChanged(event);
                break;
              }
            }
          }
        }
      }
      finally{
        commitTransaction(transaction);
      }
    }
  }

  private Pair<PomModelAspect,PomTransaction> getBlockingTransaction(final PomModelAspect aspect, PomTransaction transaction) {
    final List<PomModelAspect> allDependants = getAllDependants(aspect);
    final Iterator<PomModelAspect> iterator = allDependants.iterator();
    while (iterator.hasNext()) {
      final PomModelAspect pomModelAspect = iterator.next();
      final ListIterator<Pair<PomModelAspect, PomTransaction>> blocksIterator = myBlockedAspects.listIterator(myBlockedAspects.size());
      while(blocksIterator.hasPrevious()){
        final Pair<PomModelAspect, PomTransaction> pair = blocksIterator.previous();
        if (pomModelAspect == pair.getFirst() && // aspect dependance
            PsiTreeUtil.isAncestor(pair.getSecond().getChangeScope(), transaction.getChangeScope(), false) // target scope contain current
        ) {
          return pair;
        }
      }
    }
    return null;
  }

  private void commitTransaction(final PomTransaction transaction) {
    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    final PsiDocumentManagerImpl manager = (PsiDocumentManagerImpl)PsiDocumentManager.getInstance(myPomProject.getPsiProject());
    final PsiToDocumentSynchronizer synchronizer = manager.getSynchronizer();
    Document document = null;
    final PsiFile containingFileByTree = getContainingFileByTree(transaction.getChangeScope());
    if (containingFileByTree != null) {
      document = manager.getCachedDocument(containingFileByTree);
    }
    if(document != null) synchronizer.commitTransaction(document);
    if(progressIndicator != null) progressIndicator.finishNonCancelableSection();
  }

  private void startTransaction(final PomTransaction transaction) {
    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    if(progressIndicator != null) progressIndicator.startNonCancelableSection();
    final PsiDocumentManagerImpl manager = (PsiDocumentManagerImpl)PsiDocumentManager.getInstance(myPomProject.getPsiProject());
    final PsiToDocumentSynchronizer synchronizer = manager.getSynchronizer();
    Document document = null;
    final PsiElement changeScope = transaction.getChangeScope();
    sendPsiBeforeEvent(transaction.getChangeScope());
    LOG.assertTrue(changeScope != null);
    final PsiFile containingFileByTree = getContainingFileByTree(changeScope);

    if(containingFileByTree != null) {
      document = manager.getCachedDocument(containingFileByTree);
    }
    if(document != null) synchronizer.startTransaction(document, transaction.getChangeScope());
  }

  private PsiFile getContainingFileByTree(final PsiElement changeScope) { // there could be pseudo phisical trees (JSPX/JSP/etc.) which should not translate any changes to document and should not pass any PSI events
    final PsiFile containingFile = (PsiFile)TreeUtil.getFileElement((TreeElement)changeScope.getNode()).getPsi();
    return containingFile;
  }

  private void sendPsiBeforeEvent(final PsiElement scope) {
    if(!scope.isPhysical()) return;
    final PsiManagerImpl manager = (PsiManagerImpl)scope.getManager();
    PsiTreeChangeEventImpl event = new PsiTreeChangeEventImpl(manager);
    event.setParent(scope);
    event.setFile(scope.getContainingFile());
    event.setOffset(0);
    event.setOldLength(scope.getTextLength());
    manager.beforeChildrenChange(event);
  }

  private PomModelListener[] myListenersArray = null;
  private PomModelListener[] getListeners(){
    if(myListenersArray != null) return myListenersArray;
    return myListenersArray = myListeners.toArray(new PomModelListener[myListeners.size()]);
  }
  // Project component

  public void projectOpened() {}
  public void projectClosed() {}
  public String getComponentName() {
    return "PomModel";
  }
  public void initComponent() {}
  public void disposeComponent() {}
}