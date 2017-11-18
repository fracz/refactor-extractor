/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.k2js.translate.expression;

import com.google.dart.compiler.backend.js.ast.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ConstructorDescriptor;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.psi.JetClassBody;
import org.jetbrains.jet.lang.psi.JetClassOrObject;
import org.jetbrains.jet.lang.psi.JetDeclarationWithBody;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.k2js.translate.LabelGenerator;
import org.jetbrains.k2js.translate.context.AliasingContext;
import org.jetbrains.k2js.translate.context.Namer;
import org.jetbrains.k2js.translate.context.TranslationContext;
import org.jetbrains.k2js.translate.context.UsageTracker;
import org.jetbrains.k2js.translate.declaration.ClassTranslator;
import org.jetbrains.k2js.translate.general.AbstractTranslator;

import static org.jetbrains.k2js.translate.utils.BindingUtils.getFunctionDescriptor;
import static org.jetbrains.k2js.translate.utils.FunctionBodyTranslator.translateFunctionBody;
import static org.jetbrains.k2js.translate.utils.JsDescriptorUtils.getExpectedReceiverDescriptor;

public class LiteralFunctionTranslator extends AbstractTranslator {
    private static final LabelGenerator FUNCTION_NAME_GENERATOR = new LabelGenerator('f');

    private final JetDeclarationWithBody declaration;
    private final FunctionDescriptor descriptor;
    private final JsFunction jsFunction;
    private final TranslationContext functionContext;
    private final boolean inConstructorOrTopLevel;
    private final ClassDescriptor outerClass;
    private final JsName receiverName;

    private LiteralFunctionTranslator(@NotNull JetDeclarationWithBody declaration, @NotNull TranslationContext context) {
        super(context);

        this.declaration = declaration;
        this.descriptor = getFunctionDescriptor(context().bindingContext(), declaration);

        DeclarationDescriptor receiverDescriptor = getExpectedReceiverDescriptor(descriptor);
        jsFunction = new JsFunction(context().scope(), new JsBlock());

        AliasingContext aliasingContext;
        if (receiverDescriptor == null) {
            receiverName = null;
            aliasingContext = null;
        }
        else {
            receiverName = jsFunction.getScope().declareName(Namer.getReceiverParameterName());
            aliasingContext = context().aliasingContext().inner(receiverDescriptor, receiverName.makeRef());
        }

        if (descriptor.getContainingDeclaration() instanceof ConstructorDescriptor) {
            // KT-2388
            inConstructorOrTopLevel = true;
            jsFunction.setName(jsFunction.getScope().declareName(Namer.CALLEE_NAME));
            outerClass = (ClassDescriptor) descriptor.getContainingDeclaration().getContainingDeclaration();
            assert outerClass != null;

            if (receiverDescriptor == null) {
                aliasingContext = context().aliasingContext().notShareableThisAliased(outerClass, new JsNameRef("o", jsFunction.getName().makeRef()));
            }
        }
        else {
            outerClass = null;
            inConstructorOrTopLevel = DescriptorUtils.isTopLevelDeclaration(descriptor);
        }

        UsageTracker funTracker = new UsageTracker(descriptor, context().usageTracker(), outerClass);
        functionContext = context().newFunctionBody(jsFunction, aliasingContext, funTracker);
    }

    private void translateBody() {
        JsBlock functionBody = translateFunctionBody(descriptor, declaration, functionContext);
        jsFunction.getBody().getStatements().addAll(functionBody.getStatements());
    }

    @NotNull
    private JsExpression finish() {
        JsExpression result;

        if (inConstructorOrTopLevel) {
            result = jsFunction;

            if (outerClass != null) {
                UsageTracker usageTracker = functionContext.usageTracker();
                assert usageTracker != null;
                if (usageTracker.isUsed()) {
                    result = new JsInvocation(context().namer().kotlin("assignOwner"), jsFunction, JsLiteral.THIS);
                }
                else {
                    jsFunction.setName(null);
                }
            }
        }
        else {
            JsNameRef funReference = context().define(FUNCTION_NAME_GENERATOR.generate(), jsFunction);

            InnerFunctionTranslator innerTranslator = new InnerFunctionTranslator(descriptor, functionContext, jsFunction);
            result = innerTranslator.translate(funReference, context());
        }

        addRegularParameters(descriptor, jsFunction, functionContext, receiverName);

        return result;
    }

    @NotNull
    private JsExpression translate() {
        translateBody();
        return finish();
    }

    private static void addRegularParameters(
            @NotNull FunctionDescriptor descriptor,
            @NotNull JsFunction fun,
            @NotNull TranslationContext funContext,
            @Nullable JsName receiverName
    ) {
        if (receiverName != null) {
            fun.getParameters().add(new JsParameter(receiverName));
        }
        FunctionTranslator.addParameters(fun.getParameters(), descriptor, funContext);
    }

    @NotNull
    public static JsExpression translate(@NotNull JetDeclarationWithBody declaration, @NotNull TranslationContext outerContext) {
        return new LiteralFunctionTranslator(declaration, outerContext).translate();
    }

    // TODO: Probably should be moved to other place
    @NotNull
    public static JsExpression translate(
            @NotNull ClassDescriptor outerClass,
            @NotNull TranslationContext outerClassContext,
            @NotNull JetClassOrObject declaration,
            @NotNull ClassDescriptor descriptor,
            @NotNull ClassTranslator classTranslator
    ) {
        JsFunction fun = new JsFunction(outerClassContext.scope(), new JsBlock());
        JsNameRef outerClassRef = fun.getScope().declareName(Namer.OUTER_CLASS_NAME).makeRef();
        UsageTracker usageTracker = new UsageTracker(descriptor, outerClassContext.usageTracker(), outerClass);
        AliasingContext aliasingContext = outerClassContext.aliasingContext().inner(outerClass, outerClassRef);
        TranslationContext funContext = outerClassContext.newFunctionBody(fun, aliasingContext, usageTracker);

        fun.getBody().getStatements().add(new JsReturn(classTranslator.translate(funContext)));

        JetClassBody body = declaration.getBody();
        assert body != null;

        JsNameRef define = funContext.define(FUNCTION_NAME_GENERATOR.generate(), fun);
        return new InnerObjectTranslator(funContext, fun).translate(define, usageTracker.isUsed() ? outerClassRef : null);
    }
}