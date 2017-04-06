commit e77a5337bcad26d0f8e12cfb55ad3b1f1335e968
Merge: 4dc197c 0f86a33
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 10 11:24:41 2012 +0200

    merged branch Tobion/patch-4 (PR #5293)

    Commits
    -------

    0f86a33 micro-optim: replace for with foreach
    4efb9fe [Form] Remove unneeded FormUtil constants

    Discussion
    ----------

    [Form] Remove FormUtil constants

    The constants are not useful from outside the class as the $pluralMap is also private. So no need to expose these veriables in the API when they cannot be used in any way. Unfortunately there are not private constants, so use private static. Then I realized the variables can be removed altogether, as they are only used once anyway and the index meaning is already documented in pluralMap.

    ---------------------------------------------------------------------------

    by empire at 2012-08-18T12:58:22Z

    FormUtils is abstract class, and maybe subclass (in future) will use this constants, I think changing access modifier to `protected` is better option.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T12:59:40Z

    They cannot, as pluralMap is private...

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T14:11:17Z

    extract self::$pluralMap into local variable add small speed up
    4.5499801635742 ms vs 5.7430267333984 ms on 100 iterations

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T14:16:47Z

    This is not about performance.

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T14:21:11Z

    yes but adds
    your changes vs current is
    5.7430267333984 ms vs 6.4971446990967 ms on 100 iterations

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T14:29:48Z

    How about `$map =& self::$pluralMap[$i]`?

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T14:59:57Z

    I mean https://gist.github.com/3387253

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T15:01:45Z

    foreach is event faster :)
    (4.1971206665039 ms on my hw)

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T15:04:51Z

    I see.

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T15:06:41Z

    in first comment i mean code like this:
    ```php
    $pluralMap = self::$pluralMap; // do this because access to static property is to slow
    ```

    on my machine & is slower `$map =& $pluralMap[$i]` vs `$map = $pluralMap[$i]`
    5.0 vs 4.8 ms

    imho & not needed in read only code

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T15:15:03Z

    Well, you'd need to benchmark memory too. `=&` should reduce memory primarily in this case.

    ---------------------------------------------------------------------------

    by Partugal at 2012-08-18T15:19:35Z

    ```php
    echo memory_get_usage() . "\n"; // 711536

    $a = array_fill(5, 6000, 'banana');
    echo memory_get_usage() . "\n"; // 1497632

    $b = $a;
    echo memory_get_usage() . "\n"; // 1497760

    $b[1] = 2;
    echo memory_get_usage() . "\n"; // 2283832
    ```

    1497760  - 1497632 = 128  it is size for variable structure not for its value:

    ```php
    echo memory_get_usage() . "\n"; // 711536

    $a = 1;
    echo memory_get_usage() . "\n"; // 1497632

    $b = &$a;
    echo memory_get_usage() . "\n"; // 1497760
    ```

    ---------------------------------------------------------------------------

    by Seldaek at 2012-08-18T17:52:32Z

    @Tobion http://schlueters.de/blog/archives/125-Do-not-use-PHP-references.html - search for "copy-on-write" if you don't want to read it all.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-18T19:37:44Z

    Yeah I know about copy on write. I thought there might be a difference what you assign a sub-element of an array to a variable. But apparently not.
    Interestingly `$a =& $b` takes a little more memory than `$a = $b` according to `memory_get_usage ()` but not when using `memory_get_usage (true)`.

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-30T08:15:01Z

    I don't like the removal of the constants. They introduce meaning into the integers and improve code clarity. The rest looks good.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-30T13:18:19Z

    My opinion of the constants:
    - They are part of the public API (as const are alwalys public) but cannot be used at all, as everything else is private...
    - They are each only used once.
    - The meaning of the indices is already documented in `$pluralMap`
    - They are not used when building `$pluralMap` so they dont imprivate code clarity and consistence either. But doing so would on the other hand make it probably more ugly. So removing them is IMO best solution.

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-30T15:21:03Z

    If you really need to remove the constants, then please comment the code where they are used accordingly.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-31T00:58:51Z

    I dont see what I should comment to make it more understandable, as the the map is already assigned to a named variable like `$suffixLength = $map[1];`.

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-31T09:12:18Z

    > I dont see what I should comment to make it more understandable, as the the map is already assigned to a named variable

    `$map[2]` and `$map[3]` is not self-describing.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-31T17:23:15Z

    @bschussek Done.

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-31T22:13:41Z

    Could you please squash your commits?