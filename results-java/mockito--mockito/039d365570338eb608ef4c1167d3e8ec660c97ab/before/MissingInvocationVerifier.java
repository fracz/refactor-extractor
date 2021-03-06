/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification;

import java.util.List;

import org.mockito.exceptions.Reporter;
import org.mockito.internal.invocation.ActualInvocationsFinder;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.InvocationsAnalyzer;
import org.mockito.internal.invocation.InvocationsPrinter;
import org.mockito.internal.progress.VerificationModeImpl;

public class MissingInvocationVerifier implements Verifier {

    private final Reporter reporter;
    private final InvocationsAnalyzer analyzer;
    private final ActualInvocationsFinder finder;

    public MissingInvocationVerifier() {
        this(new InvocationsAnalyzer(), new ActualInvocationsFinder(), new Reporter());
    }

    public MissingInvocationVerifier(InvocationsAnalyzer analyzer, ActualInvocationsFinder finder, Reporter reporter) {
        this.analyzer = analyzer;
        this.finder = finder;
        this.reporter = reporter;
    }

    public void verify(List<Invocation> invocations, InvocationMatcher wanted, VerificationModeImpl mode) {
        if (!mode.missingMethodMode()) {
            return;
        }

        List<Invocation> actualInvocations = finder.findInvocations(invocations, wanted, mode);

        if (actualInvocations.size() == 0) {
            //TODO add test to check that invocations are passed here, not actual...
            Invocation similar = analyzer.findSimilarInvocation(invocations, wanted, mode);
            reportMissingInvocationError(wanted, similar);
        }

        for (Invocation invocation : actualInvocations) {
            invocation.markVerified();
            //TODO dodgy!
            if (mode.strictMode() && mode.atLeastOnceMode()) {
                invocation.markVerifiedStrictly();
            }
        }
    }

    private void reportMissingInvocationError(InvocationMatcher wanted, Invocation similar) {
        if (similar != null) {
            //TODO I want a functional test that proves that correct stack trace is provided for cause for both strictly and ordinary verification
            InvocationsPrinter printer = new InvocationsPrinter(wanted, similar);
            reporter.wantedInvocationDiffersFromActual(printer.printWanted(), printer.printActual(), similar.getStackTrace());
        } else {
            //TODO I really want a cause here, something like: "wanted after..."
            reporter.wantedButNotInvoked(wanted.toString());
        }
    }
}