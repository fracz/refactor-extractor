commit 7786fe44c55af61b4d8d38d1927b3fa13b81ad9f
Merge: aa3e474 bde67f0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 29 10:15:05 2013 +0200

    merged branch fabpot/class-not-found-exception (PR #8553)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Debug] Developer friendly Class Not Found and Undefined Function errors

    This is a followup of #8156

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8156
    | License       | MIT
    | Doc PR        | n/a

    Here is the original description from #8156:

    Per a discussion with @weaverryan and others, I took a crack at enhancing the exception display for class not found errors and undefined function errors. It is not the cleanest solution but this is a work in progress to see whether or not following this path makes sense.

    # Class Names

    ## Class Not Found: Unknown Class (Global Namespace)

    ```php
    <?php
    new WhizBangFactory();
    ```

    > Attempted to load class 'WhizBangFactory' from the global namespace in foo.php line 12. Did you forget a use statement for this class?

    ## Class Not Found: Unknown Class (Full Namespace)

    ```php
    <?php
    namespace Foo\Bar;
    new WhizBangFactory();
    ```

    > Attempted to load class 'WhizBangFactory' from namespace 'Foo\Bar' in foo.php line 12. Do you need to 'use' it from another namespace?

    ## Class Not Found: Well Known Class (Global Namespace)

    ```php
    <?php
    new Request();
    ```

    > Attempted to load class 'Request' from the global namespace in foo.php line 12. Did you forget a use statement for this class? Perhaps you need to add 'use Symfony\Component\HttpFoundation\Request' at the top of this file?

    ## Class Not Found: Well Known Class (Full Namespace)

    ```php
    <?php
    namespace Foo\Bar;
    new Request();
    ```

    > Attempted to load class 'Request' from namespace 'Foo\Bar' in foo.php line 12. Do you need to 'use' it from another namespace? Perhaps you need to add 'use Symfony\Component\HttpFoundation\Request' at the top of this file?

    # Functions

    ## Undefined Function (Global Namespace)

    ```php
    <?php
    // example.php:
    // namespace Acme\Example;
    // function test_namespaced_function()
    // {
    // }
    include "example.php";

    test_namespaced_function()
    ```

    > Attempted to call function 'test_namespaced_function' from the global namespace in foo.php line 12. Did you mean to call: '\acme\example\test_namespaced_function'?

    ## Undefined Function (Full Namespace)

    ```php
    <?php
    namespace Foo\Bar\Baz;

    // example.php:
    // namespace Acme\Example;
    // function test_namespaced_function()
    // {
    // }
    include "example.php";

    test_namespaced_function()
    ```

    > Attempted to call function 'test_namespaced_function' from namespace 'Foo\Bar\Baz' in foo.php line 12. Did you mean to call: '\acme\example\test_namespaced_function'?

    ## Undefined Function: Unknown Function (Global Namespace)

    ```php
    <?php
    test_namespaced_function()
    ```

    > Attempted to call function 'test_namespaced_function' from the global namespace in foo.php line 12.

    ## Undefined Function: Unknown Function (Full Namespace)

    ```php
    <?php
    namespace Foo\Bar\Baz;

    test_namespaced_function()
    ```

    > Attempted to call function 'test_namespaced_function' from namespace 'Foo\Bar\Baz' in foo.php line 12.

    Commits
    -------

    bde67f0 fixed an error message
    80e19e2 [Debug] added some missing phpdocs
    968764b [Debug] refactored unit tests
    cefa1b5 [Debug] moved special fatal error handlers to their own classes
    53ab284 [Debug] made Debug find FQCN automatically based on well-known autoloaders
    208ca5f [Debug] made guessing of possible class names more flexible
    a0b1585 [Debug] fixed CS
    6671945 Developer friendly Class Not Found and Undefined Function errors.