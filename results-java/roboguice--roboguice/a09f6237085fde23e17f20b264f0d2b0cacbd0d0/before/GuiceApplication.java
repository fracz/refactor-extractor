/*
 * Copyright 2009 Michael Burton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package roboguice.application;

import java.util.ArrayList;
import java.util.List;

import roboguice.config.AbstractAndroidModule;
import roboguice.config.RoboGuiceModule;
import roboguice.inject.ContextScope;
import roboguice.inject.ExtrasListener;
import roboguice.inject.InjectorProvider;
import roboguice.inject.ResourceListener;
import roboguice.inject.StaticTypeListener;
import roboguice.inject.ViewListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;

import android.app.Application;
import android.content.Context;

/**
 * This class is in charge of starting the Guice configuration. When the
 * {@link #getInjector()} method is called for the first time, a new Injector is
 * created, and the magic begins !<br />
 * <br />
 * To add your own custom bindings, you should override this class and override
 * the {@link #addApplicationModules(List)} method. <br />
 * <br />
 * You must define this class (or any subclass) as the application in your
 * <strong>AndroidManifest.xml</strong> file. This can be done by adding {@code
 * android:name="fully qualified name of your application class"} to the {@code
 * <application/>} tag. <br />
 * <br />
 * For instance : <br /> {@code <application android:icon="@drawable/icon"
 * android:label="@string/app_name"
 * android:name="roboguice.application.GuiceApplication"> [...] </application> }
 *
 * @see GuiceInjectableApplication How to get your Application injected as well.
 *
 * @author Mike Burton
 */
public class GuiceApplication extends Application implements InjectorProvider {

    /**
     * The {@link Injector} of your application.
     */
    protected Injector                 guice;

    protected ContextScope             contextScope;
    protected Provider<Context>        throwingContextProvider;
    protected Provider<Context>        contextProvider;
    protected ResourceListener         resourceListener;
    protected ViewListener             viewListener;
    protected ExtrasListener           extrasListener;
    protected List<StaticTypeListener> staticTypeListeners;

    /**
     * Returns the {@link Injector} of your application. If none exists yet,
     * creates one by calling {@link #createInjector()}. <br />
     * <br />
     * This method is thread-safe.<br />
     * <br />
     * If you decide to override {@link #getInjector()}, you will have to handle
     * synchronization.
     */
    public Injector getInjector() {
        if (guice == null) {
            synchronized (this) {
                if (guice == null) {
                    initInstanceMembers();
                    guice = createInjector();
                }
            }
        }
        return guice;
    }

    /**
     * Since we don't create the injector when the {@link GuiceApplication} is
     * instantiated, but rather when getInjector is first called (lazy
     * initialization), the same lazy initialization is applied to this
     * application instance members, which are not used until the injector is
     * first created. The main advantage is that roboguice footprint is close
     * to zero if no GuiceActivity is used when running the application.
     */
    protected void initInstanceMembers() {
        contextScope = new ContextScope();
        throwingContextProvider = ContextScope.<Context> seededKeyProvider();
        contextProvider = contextScope.scope(Key.get(Context.class), throwingContextProvider);
        resourceListener = new ResourceListener(this);
        viewListener = new ViewListener(contextProvider, this, contextScope);
        extrasListener = new ExtrasListener(contextProvider);
        staticTypeListeners = new ArrayList<StaticTypeListener>();
        staticTypeListeners.add(resourceListener);
    }

    /**
     * Creates an {@link Injector} configured for this application. This
     * {@link Injector} will be configured with a {@link RoboGuiceModule} , plus
     * any {@link Module} you might add by overriding
     * {@link #addApplicationModules(List)}. <br />
     * <br />
     * In most cases, you should <strong>NOT</strong> override the
     * {@link #createInjector()} method.
     */
    protected Injector createInjector() {
        ArrayList<Module> modules = new ArrayList<Module>();
        Module roboguiceModule = new RoboGuiceModule(contextScope, throwingContextProvider, contextProvider, resourceListener, viewListener, extrasListener,
                this);
        modules.add(roboguiceModule);
        addApplicationModules(modules);
        for (Module m : modules) {
            if (m instanceof AbstractAndroidModule) {
                ((AbstractAndroidModule) m).setStaticTypeListeners(staticTypeListeners);
            }
        }
        return Guice.createInjector(Stage.PRODUCTION, modules);
    }

    /**
     * You should override this method to add your own custom bindings. <br />
     * To do so, you must create implementations of the {@link Module}
     * interface, and add them to the list of {@link Module} given as a
     * parameter. The easiest way to create an {@link Module} implementation is
     * to subclass {@link AbstractAndroidModule}, which provides proxy methods
     * to the binder methods (enabling more readable configuration)<br />
     * <br />
     * This method is called by {@link #createInjector()}.<br />
     * <br />
     * The default implementation is a no-op and does nothing.
     *
     * @param modules
     *            The list of modules to which you may add your own custom
     *            modules. Please notice that it already contains one module,
     *            which is this.
     */
    protected void addApplicationModules(List<Module> modules) {
    }

    public List<StaticTypeListener> getStaticTypeListeners() {
        return staticTypeListeners;
    }
}