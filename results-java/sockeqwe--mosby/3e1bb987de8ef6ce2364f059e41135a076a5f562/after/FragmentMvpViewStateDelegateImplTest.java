/*
 * Copyright 2017 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hannesdorfmann.mosby3.mvp.delegate;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.hannesdorfmann.mosby3.mvp.viewstate.ViewState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Hannes Dorfmann
 */
@RunWith(PowerMockRunner.class) @PrepareForTest({ Fragment.class })
public class FragmentMvpViewStateDelegateImplTest {

  // TODO write test for retaining fragment

  private MvpView view;
  private MvpPresenter<MvpView> presenter;
  private MvpViewStateDelegateCallback<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>> callback;
  private FragmentMvpViewStateDelegateImpl<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
      delegate;
  private Fragment fragment;
  private FragmentActivity activity;
  private Application application;
  private ViewState<MvpView> viewState;

  @Before public void initComponents() {
    view = new MvpView() {
    };

    viewState = Mockito.mock(ViewState.class);

    presenter = Mockito.mock(MvpPresenter.class);
    callback = Mockito.mock(PartialMvpViewStateDelegateCallbackImpl.class);
    Mockito.doCallRealMethod().when(callback).setPresenter(presenter);
    Mockito.doCallRealMethod().when(callback).getPresenter();
    Mockito.doCallRealMethod().when(callback).setViewState(viewState);
    Mockito.doCallRealMethod().when(callback).getViewState();

    fragment = PowerMockito.mock(Fragment.class);
    activity = Mockito.mock(FragmentActivity.class);
    application = Mockito.mock(Application.class);

    Mockito.when(callback.getMvpView()).thenReturn(view);
    Mockito.when(fragment.getActivity()).thenReturn(activity);

    Mockito.when(activity.getApplication()).thenReturn(application);

    Mockito.when(callback.createPresenter()).thenReturn(presenter);
    Mockito.when(callback.createViewState()).thenReturn(viewState);

    delegate = new FragmentMvpViewStateDelegateImpl<>(fragment, callback, true, true);
  }

  @Test public void appStartWithScreenOrientationChangeAndFinallyFinishing() {
    startFragment(null, 1, 1, 1, 1, 1, 0, null, 0, 1, 0);
    Bundle bundle = BundleMocker.create();
    finishFragment(bundle, 1, true, true, false);
    startFragment(bundle, 1, 2, 2, 1, 2, 1, true, 1, 1, 1);
    finishFragment(bundle, 1, false, false, true);
  }

  @Test public void appStartFinishing() {
    startFragment(null, 1, 1, 1, 1, 1, 0, null, 0, 1, 0);
    Bundle bundle = BundleMocker.create();
    finishFragment(bundle, 1, false, false, true);
    Mockito.verifyNoMoreInteractions(viewState);
  }

  @Test public void dontKeepPresenter() {
    delegate = new FragmentMvpViewStateDelegateImpl<>(fragment, callback, false, false);
    startFragment(null, 1, 1, 1, 1, 1, 0, null, 0, 1, 0);
    Bundle bundle = BundleMocker.create();
    finishFragment(bundle, 1, false, true, false);
    startFragment(null, 2, 2, 2, 2, 2, 0, null, 0, 2, 0);
    finishFragment(bundle, 2, false, false, true);
  }

  @Test
  public void appStartWithProcessDeathAndViewStateRecreationFromBundle(){
    Assert.fail("Not implemented");
  }

  @Test
  public void appStartWithViewStateFromMemoryAndBundleButPreferViewStateFromMemory(){
    Assert.fail("Not implemented");
  }

  private void startFragment(Bundle bundle, int createPresenter, int setPresenter, int attachView,
      int createViewState, int setViewState, int applyViewState,
      Boolean viewsStateRestoredFromMemory, int setRestoreViewState, int onNewViewStateInstance,
      int onViewStateInstanceRestored) {

    delegate.onAttach(activity);
    delegate.onCreate(bundle);
    delegate.onViewCreated(null, bundle);
    delegate.onActivityCreated(bundle);
    delegate.onStart();
    delegate.onResume();

    Mockito.verify(callback, Mockito.times(createPresenter)).createPresenter();
    Mockito.verify(callback, Mockito.times(setPresenter)).setPresenter(presenter);
    Mockito.verify(presenter, Mockito.times(attachView)).attachView(view);

    Mockito.verify(callback, Mockito.times(createViewState)).createViewState();
    Mockito.verify(callback, Mockito.times(setViewState)).setViewState(viewState);

    if (viewsStateRestoredFromMemory == null) {
      Mockito.verify(viewState, Mockito.times(0)).apply(Mockito.eq(view), Mockito.eq(true));
      Mockito.verify(viewState, Mockito.times(0)).apply(Mockito.eq(view), Mockito.eq(false));
    } else {
      Mockito.verify(viewState, Mockito.times(applyViewState))
          .apply(Mockito.eq(view), Mockito.eq(viewsStateRestoredFromMemory));
    }

    Mockito.verify(callback, Mockito.times(setRestoreViewState)).setRestoringViewState(true);
    Mockito.verify(callback, Mockito.times(setRestoreViewState)).setRestoringViewState(false);

    Mockito.verify(callback, Mockito.times(onNewViewStateInstance)).onNewViewStateInstance();

    if (viewsStateRestoredFromMemory == null) {
      Mockito.verify(callback, Mockito.times(0)).onViewStateInstanceRestored(true);
      Mockito.verify(callback, Mockito.times(0)).onViewStateInstanceRestored(false);
    } else {
      Mockito.verify(callback, Mockito.times(onViewStateInstanceRestored))
          .onViewStateInstanceRestored(viewsStateRestoredFromMemory);
    }
  }

  private void finishFragment(Bundle bundle, int detachViewCount, boolean expectKeepPresenter,
      boolean changingConfigurations, boolean isFinishing) {
    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(activity.isChangingConfigurations()).thenReturn(changingConfigurations);
    Mockito.when(activity.isFinishing()).thenReturn(isFinishing);

    delegate.onPause();
    delegate.onSaveInstanceState(bundle);
    delegate.onStop();
    delegate.onDestroyView();
    delegate.onDestroy();
    delegate.onDetach();

    Mockito.verify(presenter, Mockito.times(detachViewCount)).detachView(expectKeepPresenter);
  }
}