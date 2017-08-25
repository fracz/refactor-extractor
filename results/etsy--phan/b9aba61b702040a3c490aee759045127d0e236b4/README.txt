commit b9aba61b702040a3c490aee759045127d0e236b4
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sun Jun 4 16:21:42 2017 -0700

    Make the analysis of traits much more similar to that of classes

    There were a lot of unexpected workarounds.
    Hopefully, with new features and bug fixes in Phan, and improved handling of traits, these workarounds can be removed without much user impact.

    - Start analyzing `return` statements in the inner body of trait methods.
      (Compare against the real return type if it exists,
      instead of the phpdoc return type to maintain a low false positive rate.
      @morria - Thoughts? What were the original motivations for this behavior?)
    - Get rid of workarounds to reduce false positive rate for traits
    - Set the real return type for trait methods returning `self`
      Without it being set, incorrect `return` statements wouldn't be
      detected, method override compatibility checks would be wrong, and analysis of
      chained method calls wouldn't detect some issues (See test 0195)

      Abstract methods or the `@method` tag can be used to work around false
      positives (e.g. for PhanUndeclaredMethod) in traits with many users.

    Fixes #800