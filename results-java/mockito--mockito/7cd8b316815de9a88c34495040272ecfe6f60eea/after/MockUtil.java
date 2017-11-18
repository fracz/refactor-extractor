/*
 * Copyright (c) 2007 Szczepan Faber
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal;

import net.sf.cglib.proxy.*;

import org.mockito.exceptions.NotAMockException;
import org.mockito.internal.ClassProxyFactory.MockMethodInterceptor;

public class MockUtil {

    private static MockMethodInterceptor getInterceptor(Object mock) {
        Factory factory = (Factory) mock;
        return (MockMethodInterceptor) factory.getCallback(0);
    }

    @SuppressWarnings("unchecked")
    public static <T> MockitoControl<T> getControl(T mock) {
        if (mock == null) {
            throw new IllegalArgumentException("Mock cannot be null");
        }

        MockitoObjectMethodsFilter<MockitoControl<T>> handler;

        try {
            if (Enhancer.isEnhanced(mock.getClass())) {
                handler = (MockitoObjectMethodsFilter) getInterceptor(mock)
                        .getHandler();
            } else {
                throw new NotAMockException(mock);
            }

            return handler.getDelegate();
        } catch (ClassCastException e) {
            throw new NotAMockException(mock);
        }
    }

    public static void validateMock(Object mock) {
        getControl(mock);
    }
}