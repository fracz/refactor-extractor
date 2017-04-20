commit 008fbcf8e2e0633733baba279fd2fc82593a7344
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Fri Jul 19 09:37:02 2013 -0500

    [#4822] CS

    - If we're going to make readability changes, let's make them consistent
      with the rest of the framework:
      - array arguments should all have commas following each entry
      - array arguments as sole argument of a method can open on same line
        as call; indentation then adjusts one level less
    - Use `assertInstanceOf` throughout, instead of `assertTrue($a instanceof $b)`
    - While the `setExpectedException()` lines get a little long, they're
      not unreadable; breaking them into multiple lines makes intent less
      clear.