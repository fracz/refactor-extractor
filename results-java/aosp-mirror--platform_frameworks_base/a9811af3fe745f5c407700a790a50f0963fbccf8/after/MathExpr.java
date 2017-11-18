/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.databinding2.expr;

import com.android.databinding2.ClassAnalyzer;

import java.util.List;

public class MathExpr extends Expr {
    final String mOp;
    final Expr mLeft;
    final Expr mRight;
    MathExpr(Expr left, String op, Expr right) {
        super(left, right);
        mOp = op;
        mLeft = left;
        mRight = right;
    }

    @Override
    protected String computeUniqueKey() {
        return sUniqueKeyJoiner.join(mLeft.getUniqueKey(), mOp, mRight.getUniqueKey());
    }

    @Override
    protected Class resolveType(ClassAnalyzer classAnalyzer) {
        if ("+".equals(mOp)) {
            // TODO we need upper casting etc.
            if (String.class.equals(mLeft.getResolvedType())
                    || String.class.equals(mRight.getResolvedType())) {
                return String.class;
            }
        }
        return classAnalyzer.findCommonParentOf(mLeft.getResolvedType(), mRight.getResolvedType());
    }

    @Override
    protected List<Dependency> constructDependencies() {
        return constructDynamicChildrenDependencies();
    }

    public String getOp() {
        return mOp;
    }

    public Expr getLeft() {
        return mLeft;
    }

    public Expr getRight() {
        return mRight;
    }
}