/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.internal.progress.VerificationModeImpl.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.RequiresValidState;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.HasStackTrace;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.InvocationsAnalyzer;
import org.mockito.internal.progress.VerificationModeImpl;

public class MissingInvocationVerifierTest extends RequiresValidState {

    private MissingInvocationVerifier verifier;

    private InvocationsAnalyzerStub analyzerStub;
    private ActualInvocationsFinderStub finderStub;
    private ReporterStub reporterStub;

    private InvocationMatcher wanted;
    private List<Invocation> invocations;

    @Before
    public void setup() {
        analyzerStub = new InvocationsAnalyzerStub();
        reporterStub = new ReporterStub();
        finderStub = new ActualInvocationsFinderStub();
        verifier = new MissingInvocationVerifier(analyzerStub, finderStub, reporterStub);

        wanted = new InvocationBuilder().toInvocationMatcher();
        invocations = asList(new InvocationBuilder().toInvocation());
    }

    @Test
    public void shouldNeverVerifyWhenModeIsNotMissingMethodMode() {
        verifier.verify(null, null, noMoreInteractions());
    }

    @Test
    public void shouldAskFinderForActualInvocations() {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        VerificationModeImpl mode = atLeastOnce();
        verifier.verify(invocations, wanted, mode);

        assertSame(invocations, finderStub.invocations);
    }

    @Test
    public void shouldPassBecauseActualInvocationFound() {
        finderStub.actualToReturn.add(new InvocationBuilder().toInvocation());
        verifier.verify(invocations, wanted, atLeastOnce());
    }

    @Test
    public void shouldAskAnalyzerForSimilarInvocation() {
        verifier.verify(invocations, wanted, VerificationModeImpl.atLeastOnce());

        assertSame(invocations, analyzerStub.invocations);
    }

    @Test
    public void shouldReportWantedButNotInvoked() {
        assertTrue(finderStub.actualToReturn.isEmpty());
        analyzerStub.similarToReturn = null;

        verifier.verify(invocations, wanted, VerificationModeImpl.atLeastOnce());

        assertEquals(wanted.toString(), reporterStub.wanted);
    }

    @Test
    public void shouldReportWantedInvocationDiffersFromActual() {
        assertTrue(finderStub.actualToReturn.isEmpty());
        Invocation actualInvocation = new InvocationBuilder().toInvocation();
        analyzerStub.similarToReturn = actualInvocation;

        verifier.verify(invocations, wanted, VerificationModeImpl.atLeastOnce());

        assertEquals(wanted.toString(), reporterStub.wanted);
        assertEquals(actualInvocation.toString(), reporterStub.actual);
        assertSame(actualInvocation.getStackTrace(), reporterStub.actualInvocationStackTrace);
    }

    class InvocationsAnalyzerStub extends InvocationsAnalyzer {
        private Invocation similarToReturn;
        private List<Invocation> invocations;

        @Override public Invocation findSimilarInvocation(List<Invocation> invocations, InvocationMatcher wanted, VerificationModeImpl mode) {
            this.invocations = invocations;
            return similarToReturn;
        }
    }

    class ReporterStub extends Reporter {
        private String wanted;
        private String actual;
        private HasStackTrace actualInvocationStackTrace;
        @Override public void wantedButNotInvoked(String wanted) {
            this.wanted = wanted;
        }
        @Override public void wantedInvocationDiffersFromActual(String wanted, String actual, HasStackTrace actualInvocationStackTrace) {
                    this.wanted = wanted;
                    this.actual = actual;
                    this.actualInvocationStackTrace = actualInvocationStackTrace;
        }
    }
}