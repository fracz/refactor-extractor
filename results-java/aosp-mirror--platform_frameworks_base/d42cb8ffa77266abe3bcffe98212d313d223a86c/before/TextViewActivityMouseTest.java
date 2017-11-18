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
 * limitations under the License
 */

package android.widget;

import static android.widget.espresso.TextViewActions.mouseDoubleClickOnTextAtIndex;
import static android.widget.espresso.TextViewActions.mouseLongClickOnTextAtIndex;
import static android.widget.espresso.TextViewActions.mouseDoubleClickAndDragOnText;
import static android.widget.espresso.TextViewActions.mouseDragOnText;
import static android.widget.espresso.TextViewActions.mouseLongClickAndDragOnText;
import static android.widget.espresso.TextViewAssertions.hasSelection;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import com.android.frameworks.coretests.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Tests mouse interaction of the TextView widget from an Activity
 */
public class TextViewActivityMouseTest extends ActivityInstrumentationTestCase2<TextViewActivity>{

    public TextViewActivityMouseTest() {
        super(TextViewActivity.class);
    }

    @SmallTest
    public void testSelectTextByDrag() throws Exception {
        getActivity();

        final String helloWorld = "Hello world!";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(helloWorld));
        onView(withId(R.id.textview)).perform(
                mouseDragOnText(helloWorld.indexOf("llo"), helloWorld.indexOf("ld!")));

        onView(withId(R.id.textview)).check(hasSelection("llo wor"));
    }

    @SmallTest
    public void testSelectTextByDrag_reverse() throws Exception {
        getActivity();

        final String helloWorld = "Hello world!";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(helloWorld));
        onView(withId(R.id.textview)).perform(
                mouseDragOnText( helloWorld.indexOf("ld!"), helloWorld.indexOf("llo")));

        onView(withId(R.id.textview)).check(hasSelection("llo wor"));
    }

    @SmallTest
    public void testSelectTextByLongClick() throws Exception {
        getActivity();

        final String helloWorld = "Hello world!";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(helloWorld));

        onView(withId(R.id.textview)).perform(mouseLongClickOnTextAtIndex(0));
        onView(withId(R.id.textview)).check(hasSelection("Hello"));

        onView(withId(R.id.textview)).perform(mouseLongClickOnTextAtIndex(
                helloWorld.indexOf("world")));
        onView(withId(R.id.textview)).check(hasSelection("world"));

        onView(withId(R.id.textview)).perform(mouseLongClickOnTextAtIndex(
                helloWorld.indexOf("llo")));
        onView(withId(R.id.textview)).check(hasSelection("Hello"));

        onView(withId(R.id.textview)).perform(mouseLongClickOnTextAtIndex(
                helloWorld.indexOf("rld")));
        onView(withId(R.id.textview)).check(hasSelection("world"));
    }

    @SmallTest
    public void testSelectTextByDoubleClick() throws Exception {
        getActivity();

        final String helloWorld = "Hello world!";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(helloWorld));

        onView(withId(R.id.textview)).perform(mouseDoubleClickOnTextAtIndex(0));
        onView(withId(R.id.textview)).check(hasSelection("Hello"));

        onView(withId(R.id.textview)).perform(mouseDoubleClickOnTextAtIndex(
                helloWorld.indexOf("world")));
        onView(withId(R.id.textview)).check(hasSelection("world"));

        onView(withId(R.id.textview)).perform(mouseDoubleClickOnTextAtIndex(
                helloWorld.indexOf("llo")));
        onView(withId(R.id.textview)).check(hasSelection("Hello"));

        onView(withId(R.id.textview)).perform(mouseDoubleClickOnTextAtIndex(
                helloWorld.indexOf("rld")));
        onView(withId(R.id.textview)).check(hasSelection("world"));
    }

    @SmallTest
    public void testSelectTextByDoubleClickAndDrag() throws Exception {
        getActivity();

        final String text = "abcd efg hijk lmn";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(text));

        onView(withId(R.id.textview)).perform(
                mouseDoubleClickAndDragOnText(text.indexOf("f"), text.indexOf("j")));
        onView(withId(R.id.textview)).check(hasSelection("efg hijk"));
    }

    @SmallTest
    public void testSelectTextByDoubleClickAndDrag_reverse() throws Exception {
        getActivity();

        final String text = "abcd efg hijk lmn";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(text));

        onView(withId(R.id.textview)).perform(
                mouseDoubleClickAndDragOnText(text.indexOf("j"), text.indexOf("f")));
        onView(withId(R.id.textview)).check(hasSelection("efg hijk"));
    }

    @SmallTest
    public void testSelectTextByLongPressAndDrag() throws Exception {
        getActivity();

        final String text = "abcd efg hijk lmn";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(text));

        onView(withId(R.id.textview)).perform(
                mouseLongClickAndDragOnText(text.indexOf("f"), text.indexOf("j")));
        onView(withId(R.id.textview)).check(hasSelection("efg hijk"));
    }

    @SmallTest
    public void testSelectTextByLongPressAndDrag_reverse() throws Exception {
        getActivity();

        final String text = "abcd efg hijk lmn";
        onView(withId(R.id.textview)).perform(click());
        onView(withId(R.id.textview)).perform(typeTextIntoFocusedView(text));

        onView(withId(R.id.textview)).perform(
                mouseLongClickAndDragOnText(text.indexOf("j"), text.indexOf("f")));
        onView(withId(R.id.textview)).check(hasSelection("efg hijk"));
    }
}