/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.dsl.internal.transform;

import com.google.common.collect.Lists;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.gradle.api.Nullable;
import org.gradle.groovy.scripts.internal.AstUtils;
import org.gradle.groovy.scripts.internal.RestrictiveCodeVisitor;
import org.gradle.model.internal.core.ModelPath;

import java.util.List;

public class RulesVisitor extends RestrictiveCodeVisitor {

    // TODO - have to do much better here
    public static final String INVALID_STATEMENT = "illegal rule";

    private final RuleVisitor ruleVisitor;

    public RulesVisitor(SourceUnit sourceUnit, RuleVisitor ruleVisitor) {
        super(sourceUnit, INVALID_STATEMENT);
        this.ruleVisitor = ruleVisitor;
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        for (Statement statement : block.getStatements()) {
            statement.visit(this);
        }
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        ClosureExpression closureExpression = AstUtils.getSingleClosureArg(call);
        if (closureExpression == null) {
            restrict(call);
            return;
        }

        String modelPath = extractModelPathFromMethodTarget(call);
        if (modelPath == null) {
            return;
        }

        // Rewrite the method call to match ModelDsl#configure(String, Closure), which is what the delegate will be
        ConstantExpression modelPathArgument = new ConstantExpression(modelPath);
        ArgumentListExpression replacedArgumentList = new ArgumentListExpression(modelPathArgument, closureExpression);
        call.setMethod(new ConstantExpression("configure"));
        call.setArguments(replacedArgumentList);

        // Call directly on the delegate to avoid some dynamic dispatch
        call.setImplicitThis(true);
        call.setObjectExpression(new MethodCallExpression(VariableExpression.THIS_EXPRESSION, "getDelegate", ArgumentListExpression.EMPTY_ARGUMENTS));

        closureExpression.visit(ruleVisitor);
    }

    @Nullable // if the target was invalid
    private String extractModelPathFromMethodTarget(MethodCallExpression call) {
        Expression target = call.getMethod();
        List<String> names = Lists.newLinkedList();
        while (true) {
            if (target instanceof ConstantExpression) {
                if (target.getType().equals(ClassHelper.STRING_TYPE)) {
                    String name = target.getText();
                    names.add(0, name);
                    if (call.isImplicitThis()) {
                        break;
                    } else {
                        target = call.getObjectExpression();
                        continue;
                    }
                }
            } else if (target instanceof PropertyExpression) {
                PropertyExpression propertyExpression = (PropertyExpression) target;
                Expression property = propertyExpression.getProperty();
                if (property instanceof ConstantExpression) {
                    ConstantExpression constantProperty = (ConstantExpression) property;
                    if (constantProperty.getType().equals(ClassHelper.STRING_TYPE)) {
                        String name = constantProperty.getText();
                        names.add(0, name);
                        target = propertyExpression.getObjectExpression();
                        continue;
                    }
                }
            } else if (target instanceof VariableExpression) {
                // This will be the left most property
                names.add(0, ((VariableExpression) target).getName());
                break;
            }

            // Invalid paths fall through to here

            restrict(call);
            return null;
        }

        // TODO - validate that it's a valid model path
        return ModelPath.pathString(names);
    }
}