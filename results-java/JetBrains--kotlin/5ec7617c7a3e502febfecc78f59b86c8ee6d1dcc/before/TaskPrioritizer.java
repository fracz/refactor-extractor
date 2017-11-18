/*
 * Copyright 2010-2012 JetBrains s.r.o.
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
 */

package org.jetbrains.jet.lang.resolve.calls;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetReferenceExpression;
import org.jetbrains.jet.lang.psi.JetSuperExpression;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.calls.autocasts.AutoCastServiceImpl;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ClassReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.NamespaceType;
import org.jetbrains.jet.lang.types.checker.JetTypeChecker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor.NO_RECEIVER;

/**
 * @author abreslav
 */
/*package*/ abstract class TaskPrioritizer<D extends CallableDescriptor> {

    public static <D extends CallableDescriptor> void splitLexicallyLocalDescriptors(
            @NotNull Collection<ResolvedCallImpl<D>> allDescriptors,
            @NotNull DeclarationDescriptor containerOfTheCurrentLocality,
            @NotNull Collection<ResolvedCallImpl<D>> local,
            @NotNull Collection<ResolvedCallImpl<D>> nonlocal
    ) {
        for (ResolvedCallImpl<D> resolvedCall : allDescriptors) {
            if (DescriptorUtils.isLocal(containerOfTheCurrentLocality, resolvedCall.getCandidateDescriptor())) {
                local.add(resolvedCall);
            }
            else {
                nonlocal.add(resolvedCall);
            }
        }
    }

    @Nullable
    /*package*/ static JetSuperExpression getReceiverSuper(@NotNull ReceiverDescriptor receiver) {
        if (receiver instanceof ExpressionReceiver) {
            ExpressionReceiver expressionReceiver = (ExpressionReceiver) receiver;
            JetExpression expression = expressionReceiver.getExpression();
            if (expression instanceof JetSuperExpression) {
                return (JetSuperExpression) expression;
            }
        }
        return null;
    }

    @NotNull
    public List<ResolutionTask<D>> computePrioritizedTasks(@NotNull BasicResolutionContext context, @NotNull String name,
                                                           @NotNull JetReferenceExpression functionReference) {
        ReceiverDescriptor explicitReceiver = context.call.getExplicitReceiver();
        final JetScope scope;
        if (explicitReceiver.exists() && explicitReceiver.getType() instanceof NamespaceType) {
            // Receiver is a namespace
            scope = explicitReceiver.getType().getMemberScope();
            explicitReceiver = NO_RECEIVER;
        }
        else {
            scope = context.scope;
        }
        final Predicate<ResolvedCallImpl<D>> visibleStrategy = new Predicate<ResolvedCallImpl<D>>() {
            @Override
            public boolean apply(@Nullable ResolvedCallImpl<D> call) {
                if (call == null) return false;
                D candidateDescriptor = call.getCandidateDescriptor();
                if (ErrorUtils.isError(candidateDescriptor)) return true;
                return Visibilities.isVisible(candidateDescriptor, scope.getContainingDeclaration());
            }
        };

        ResolutionTaskHolder<D> result = new ResolutionTaskHolder<D>(functionReference, context, visibleStrategy );
        doComputeTasks(scope, explicitReceiver, name, result, context);

        return result.getTasks();
    }

    private void doComputeTasks(@NotNull JetScope scope, @NotNull ReceiverDescriptor receiver,
            @NotNull String name, @NotNull ResolutionTaskHolder<D> result,
            @NotNull BasicResolutionContext context) {
        AutoCastServiceImpl autoCastService = new AutoCastServiceImpl(context.dataFlowInfo, context.trace.getBindingContext());
        List<ReceiverDescriptor> implicitReceivers = Lists.newArrayList();
        scope.getImplicitReceiversHierarchy(implicitReceivers);
        if (receiver.exists()) {
            List<ReceiverDescriptor> variantsForExplicitReceiver = autoCastService.getVariantsForReceiver(receiver);

            Collection<ResolvedCallImpl<D>> extensionFunctions = convertWithImpliedThis(scope, variantsForExplicitReceiver, getExtensionsByName(scope, name));
            List<ResolvedCallImpl<D>> nonlocals = Lists.newArrayList();
            List<ResolvedCallImpl<D>> locals = Lists.newArrayList();
            //noinspection unchecked,RedundantTypeArguments
            TaskPrioritizer.<D>splitLexicallyLocalDescriptors(extensionFunctions, scope.getContainingDeclaration(), locals, nonlocals);

            Collection<ResolvedCallImpl<D>> members = Lists.newArrayList();
            for (ReceiverDescriptor variant : variantsForExplicitReceiver) {
                Collection<D> membersForThisVariant = getMembersByName(variant.getType(), name);
                convertWithReceivers(membersForThisVariant, Collections.singletonList(variant), Collections.singletonList(NO_RECEIVER), members);
            }

            result.addLocalExtensions(locals);
            result.addMembers(members);

            for (ReceiverDescriptor implicitReceiver : implicitReceivers) {
                Collection<D> memberExtensions = getExtensionsByName(implicitReceiver.getType().getMemberScope(), name);
                List<ReceiverDescriptor> variantsForImplicitReceiver = autoCastService.getVariantsForReceiver(implicitReceiver);
                result.addNonLocalExtensions(convertWithReceivers(memberExtensions, variantsForImplicitReceiver, variantsForExplicitReceiver));
            }

            result.addNonLocalExtensions(nonlocals);
        }
        else {
            Collection<ResolvedCallImpl<D>> functions = convertWithImpliedThis(scope, Collections.singletonList(receiver), getNonExtensionsByName(scope, name));

            List<ResolvedCallImpl<D>> nonlocals = Lists.newArrayList();
            List<ResolvedCallImpl<D>> locals = Lists.newArrayList();
            //noinspection unchecked,RedundantTypeArguments
            TaskPrioritizer.<D>splitLexicallyLocalDescriptors(functions, scope.getContainingDeclaration(), locals, nonlocals);

            result.addLocalExtensions(locals);
            result.addNonLocalExtensions(nonlocals);

            for (ReceiverDescriptor implicitReceiver : implicitReceivers) {
                doComputeTasks(scope, implicitReceiver, name, result, context);
            }
        }
    }

    private Collection<ResolvedCallImpl<D>> convertWithReceivers(Collection<D> descriptors, Iterable<ReceiverDescriptor> thisObjects, Iterable<ReceiverDescriptor> receiverParameters) {
        Collection<ResolvedCallImpl<D>> result = Lists.newArrayList();
        convertWithReceivers(descriptors, thisObjects, receiverParameters, result);
        return result;
    }

    private void convertWithReceivers(Collection<D> descriptors, Iterable<ReceiverDescriptor> thisObjects, Iterable<ReceiverDescriptor> receiverParameters, Collection<ResolvedCallImpl<D>> result) {
        for (ReceiverDescriptor thisObject : thisObjects) {
            for (ReceiverDescriptor receiverParameter : receiverParameters) {
                for (D extension : descriptors) {
                    ResolvedCallImpl<D> resolvedCall = ResolvedCallImpl.create(extension);
                    resolvedCall.setThisObject(thisObject);
                    resolvedCall.setReceiverArgument(receiverParameter);
                    result.add(resolvedCall);
                }
            }
        }
    }

    public static <D extends CallableDescriptor> Collection<ResolvedCallImpl<D>> convertWithImpliedThis(JetScope scope, Iterable<ReceiverDescriptor> receiverParameters, Collection<? extends D> descriptors) {
        Collection<ResolvedCallImpl<D>> result = Lists.newArrayList();
        for (ReceiverDescriptor receiverParameter : receiverParameters) {
            for (D descriptor : descriptors) {
                ResolvedCallImpl<D> resolvedCall = ResolvedCallImpl.create(descriptor);
                resolvedCall.setReceiverArgument(receiverParameter);
                if (setImpliedThis(scope, resolvedCall)) {
                    result.add(resolvedCall);
                }
            }
        }
        for (D descriptor : descriptors) {
            if (descriptor.getExpectedThisObject().exists()) {
                DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration();
                if (descriptor instanceof ConstructorDescriptor) {
                    assert containingDeclaration != null;
                    containingDeclaration = containingDeclaration.getContainingDeclaration();
                }
                if (containingDeclaration instanceof ClassDescriptor && ((ClassDescriptor) containingDeclaration).getKind() == ClassKind.OBJECT) {
                    ResolvedCallImpl<D> resolvedCall = ResolvedCallImpl.create(descriptor);
                    resolvedCall.setThisObject(new ClassReceiver((ClassDescriptor) containingDeclaration));
                    result.add(resolvedCall);
                }
            }
        }
        return result;
    }

    private static <D extends CallableDescriptor> boolean setImpliedThis(@NotNull JetScope scope, ResolvedCallImpl<D> resolvedCall) {
        ReceiverDescriptor expectedThisObject = resolvedCall.getCandidateDescriptor().getExpectedThisObject();
        if (!expectedThisObject.exists()) return true;
        List<ReceiverDescriptor> receivers = Lists.newArrayList();
        scope.getImplicitReceiversHierarchy(receivers);
        for (ReceiverDescriptor receiver : receivers) {
            if (JetTypeChecker.INSTANCE.isSubtypeOf(receiver.getType(), expectedThisObject.getType())) {
                // TODO : Autocasts & nullability
                resolvedCall.setThisObject(expectedThisObject);
                return true;
            }
        }
        return false;
    }

    @NotNull
    protected abstract Collection<D> getNonExtensionsByName(JetScope scope, String name);

    @NotNull
    protected abstract Collection<D> getMembersByName(@NotNull JetType receiver, String name);

    @NotNull
    protected abstract Collection<D> getExtensionsByName(JetScope scope, String name);
}