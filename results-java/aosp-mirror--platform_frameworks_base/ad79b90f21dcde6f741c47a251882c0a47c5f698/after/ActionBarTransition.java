/*
 * Copyright (C) 2013 The Android Open Source Project
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


package com.android.internal.transition;

import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TextChange;
import android.transition.Transition;
import android.transition.TransitionSet;

public class ActionBarTransition {

    private static final int TRANSITION_DURATION = 120; // ms

    private static final Transition sTransition;

    static {
        final TextChange tc = new TextChange();
        tc.setChangeBehavior(TextChange.CHANGE_BEHAVIOR_OUT_IN);
        final TransitionSet inner = new TransitionSet();
        inner.addTransition(tc).addTransition(new ChangeBounds());
        final TransitionSet tg = new TransitionSet();
        tg.addTransition(new Fade(Fade.OUT)).addTransition(inner).addTransition(new Fade(Fade.IN));
        tg.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
        tg.setDuration(TRANSITION_DURATION);
        sTransition = tg;
    }

    public static Transition getActionBarTransition() {
        return sTransition;
    }
}