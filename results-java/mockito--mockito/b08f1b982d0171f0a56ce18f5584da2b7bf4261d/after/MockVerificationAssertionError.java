package org.mockito.exceptions;


public class MockVerificationAssertionError extends AssertionError {

    public MockVerificationAssertionError() {
        super("Mock verification failed");
    }
}