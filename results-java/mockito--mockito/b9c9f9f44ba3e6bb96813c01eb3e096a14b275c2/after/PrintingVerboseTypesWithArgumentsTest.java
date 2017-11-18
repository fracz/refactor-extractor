/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */

package org.mockitousage.verification;

import org.junit.Test;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PrintingVerboseTypesWithArgumentsTest extends TestBase {

    class Boo {
        public void withLong(long x) {
        }

        public void withLongAndInt(long x, int y) {
        }
    }

    @Test
    public void should_not_report_argument_types_when_to_string_is_the_same() {
        //given
        Boo boo = mock(Boo.class);
        boo.withLong(100);

        try {
            //when
            verify(boo).withLong(eq(100));
            fail();
        } catch (ArgumentsAreDifferent e) {
            //then
            assertContains("withLong((Integer) 100);", e.getMessage());
            assertContains("withLong((Long) 100);", e.getMessage());
        }
    }

    @Test
    public void should_show_the_type_of_only_the_argument_that_doesnt_match() {
        //given
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);

        try {
            //when
            verify(boo).withLongAndInt(eq(100), eq(200));
            fail();
        } catch (ArgumentsAreDifferent e) {
            //then
            assertContains("withLongAndInt((Integer) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
        }
    }

    @Test
    public void should_show_the_type_of_the_mismatching_argument_when_output_descriptions_for_invocations_are_different() {
        //given
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);

        try {
            //when
            verify(boo).withLongAndInt(eq(100), any(Integer.class));
            fail();
        } catch (ArgumentsAreDifferent e) {
            //then
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Integer) 100, <any>", e.getMessage());
        }
    }

    @Test
    public void should_not_show_types_when_argument_value_is_different() {
        //given
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);

        try {
            //when
            verify(boo).withLongAndInt(eq(100L), eq(230));
            fail();
        } catch (ArgumentsAreDifferent e) {
            //then
            assertContains("withLongAndInt(100, 200)", e.getMessage());
            assertContains("withLongAndInt(100, 230)", e.getMessage());
        }
    }

    class Foo {

        private final int x;

        public Foo(int x) {
            this.x = x;
        }

        public boolean equals(Object obj) {
            return x == ((Foo) obj).x;
        }

        public int hashCode() {
            return 1;
        }

        public String toString() {
            return "foo";
        }
    }

    @Test
    public void should_not_show_types_when_types_are_the_same_even_if_to_string_gives_the_same_result() {
        //given
        IMethods mock = mock(IMethods.class);
        mock.simpleMethod(new Foo(10));

        try {
            //when
            verify(mock).simpleMethod(new Foo(20));
            fail();
        } catch (ArgumentsAreDifferent e) {
            //then
            assertContains("simpleMethod(foo)", e.getMessage());
        }
    }
}