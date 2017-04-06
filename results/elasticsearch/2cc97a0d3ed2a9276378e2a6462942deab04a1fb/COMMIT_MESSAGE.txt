commit 2cc97a0d3ed2a9276378e2a6462942deab04a1fb
Author: Nik Everett <nik9000@gmail.com>
Date:   Fri Oct 9 13:15:22 2015 -0400

    Remove and ban @Test

    There are three ways `@Test` was used. Way one:

    ```java
    @Test
    public void flubTheBlort() {
    ```

    This way was always replaced with:

    ```java
    public void testFlubTheBlort() {
    ```

    Or, maybe with a better method name if I was feeling generous.

    Way two:

    ```java
    @Test(throws=IllegalArgumentException.class)
    public void testFoo() {
        methodThatThrows();
    }
    ```

    This way of using `@Test` is actually pretty OK, but to get the tools to ban
    `@Test` entirely it can't be used. Instead:

    ```java
    public void testFoo() {
        try {
            methodThatThrows();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e ) {
            assertThat(e.getMessage(), containsString("something"));
        }
    }
    ```

    This is longer but tests more than the old ways and is much more precise.
    Compare:

    ```java
    @Test(throws=IllegalArgumentException.class)
    public void testFoo() {
        some();
        copy();
        and();
        pasted();
        methodThatThrows();
        code();  // <---- This was left here by mistake and is never called
    }
    ```

    to:

    ```java
    @Test(throws=IllegalArgumentException.class)
    public void testFoo() {
        some();
        copy();
        and();
        pasted();
        try {
            methodThatThrows();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e ) {
            assertThat(e.getMessage(), containsString("something"));
        }
    }
    ```

    The final use of test is:

    ```java
    @Test(timeout=1000)
    public void testFoo() {
        methodThatWasSlow();
    }
    ```

    This is the most insidious use of `@Test` because its tempting but tragically
    flawed. Its flaws are:
    1. Hard and fast timeouts can look like they are asserting that something is
    faster and even do an ok job of it when you compare the timings on the same
    machine but as soon as you take them to another machine they start to be
    invalid. On a slow VM both the new and old methods fail. On a super-fast
    machine the slower and faster ways succeed.
    2. Tests often contain slow `assert` calls so the performance of tests isn't
    sure to predict the performance of non-test code.
    3. These timeouts are rude to debuggers because the test just drops out from
    under it after the timeout.

    Confusingly, timeouts are useful in tests because it'd be rude for a broken
    test to cause CI to abort the whole build after it hits a global timeout. But
    those timeouts should be very very long "backstop" timeouts and aren't useful
    assertions about speed.

    For all its flaws `@Test(timeout=1000)` doesn't have a good replacement __in__
    __tests__. Nightly benchmarks like http://benchmarks.elasticsearch.org/ are
    useful here because they run on the same machine but they aren't quick to check
    and it takes lots of time to figure out the regressions. Sometimes its useful
    to compare dueling implementations but that requires keeping both
    implementations around. All and all we don't have a satisfactory answer to the
    question "what do you replace `@Test(timeout=1000)`" with. So we handle each
    occurrence on a case by case basis.

    For files with `@Test` this also:
    1. Removes excess blank lines. They don't help anything.
    2. Removes underscores from method names. Those would fail any code style
    checks we ever care to run and don't add to readability. Since I did this manually
    I didn't do it consistently.
    3. Make sure all test method names start with `test`. Some used to end in `Test` or start
    with `verify` or `check` and they were picked up using the annotation. Without the
    annotation they always need to start with `test`.
    4. Organizes imports using the rules we generate for Eclipse. For the most part
    this just removes `*` imports which is a win all on its own. It was "required"
    to quickly remove `@Test`.
    5. Removes unneeded casts. This is just a setting I have enabled in Eclipse and
    forgot to turn off before I did this work. It probably isn't hurting anything.
    6. Removes trailing whitespace. Again, another Eclipse setting I forgot to turn
    off that doesn't hurt anything. Hopefully.
    7. Swaps some tests override superclass tests to make them empty with
    `assumeTrue` so that the reasoning for the skips is logged in the test run and
    it doesn't "look like" that thing is being tested when it isn't.
    8. Adds an oxford comma to an error message.

    The total test count doesn't change. I know. I counted.
    ```bash
    git checkout master && mvn clean && mvn install | tee with_test
    git no_test_annotation master && mvn clean && mvn install | tee not_test
    grep 'Tests summary' with_test > with_test_summary
    grep 'Tests summary' not_test > not_test_summary
    diff with_test_summary not_test_summary
    ```

    These differ somewhat because some tests are skipped based on the random seed.
    The total shouldn't differ. But it does!
    ```
    1c1
    < [INFO] Tests summary: 564 suites (1 ignored), 3171 tests, 31 ignored (31 assumptions)
    ---
    > [INFO] Tests summary: 564 suites (1 ignored), 3167 tests, 17 ignored (17 assumptions)
    ```

    These are the core unit tests. So we dig further:
    ```bash
    cat with_test | perl -pe 's/\n// if /^Suite/;s/.*\n// if /IGNOR/;s/.*\n// if /Assumption #/;s/.*\n// if /HEARTBEAT/;s/Completed .+?,//' | grep Suite > with_test_suites
    cat not_test | perl -pe 's/\n// if /^Suite/;s/.*\n// if /IGNOR/;s/.*\n// if /Assumption #/;s/.*\n// if /HEARTBEAT/;s/Completed .+?,//' | grep Suite > not_test_suites
    diff <(sort with_test_suites) <(sort not_test_suites)
    ```

    The four tests with lower test numbers are all extend `AbstractQueryTestCase`
    and all have a method that looks like this:

    ```java
    @Override
    public void testToQuery() throws IOException {
        assumeTrue("test runs only when at least a type is registered", getCurrentTypes().length > 0);
        super.testToQuery();
    }
    ```

    It looks like this method was being double counted on master and isn't anymore.

    Closes #14028