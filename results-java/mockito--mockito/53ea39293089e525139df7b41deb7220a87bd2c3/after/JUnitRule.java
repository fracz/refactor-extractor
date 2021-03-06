/*
 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.junit;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;
import org.mockito.internal.util.MockitoLogger;
import org.mockito.junit.MockitoRule;
import org.mockito.listeners.MockCreationListener;
import org.mockito.mock.MockCreationSettings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Internal implementation.
 */
public class JUnitRule implements MockitoRule {

	private final MockitoLogger logger;
    private final Collection<MockitoTestListener> listeners = new LinkedList<MockitoTestListener>();

    /**
     * @param logger target for the stubbing warnings
     * @param silent whether the rule emits warnings
     */
    public JUnitRule(MockitoLogger logger, boolean silent) {
		this.logger = logger;
        if (silent) {
            listeners.add(new SilentTestListener());
        } else {
            listeners.add(new WarningTestListener(logger));
        }
    }

	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            public void evaluate() throws Throwable {
                MockCollector mockCollector = new MockCollector();
                Mockito.framework().addListener(mockCollector);

                Throwable problem;
                try {
                    for (MockitoTestListener listener : listeners) {
                        listener.beforeTest(target, method.getName());
                    }

                    problem = evaluateSafely(base);
                } finally {
                     Mockito.framework().removeListener(mockCollector);
                }

                //If the infrastructure fails below, we don't see the original problem, thrown later
                for (MockitoTestListener listener : listeners) {
                    listener.afterTest(mockCollector.mocks, problem);
                }

                if (problem != null) {
                    throw problem;
                }
            }

            private Throwable evaluateSafely(Statement base) {
                try {
                    base.evaluate();
                    return null;
                } catch (Throwable throwable) {
                    return throwable;
                }
            }
        };
    }

    public JUnitRule silent() {
        return new JUnitRule(logger, true);
    }

    private static class MockCollector implements MockCreationListener {
        private final List<Object> mocks = new LinkedList<Object>();

        public void onMockCreated(Object mock, MockCreationSettings settings) {
            mocks.add(mock);
        }
    }

}