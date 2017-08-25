commit 9e5351afaace3dd8c4366f9e70ff2da77a371d03
Author: Nate Abele <nate.abele@gmail.com>
Date:   Mon May 4 15:14:45 2015 -0400

    Ensure primary record set stays in order.

    Co-auhored by David Persson
    Tests by HamidReza Koushki

    Closes #1189
    Refs #1162

    - Misc. cleanup and refactoring

    - Remove result caching from database iterators.

    - Implement `peek()`

    - Fix incorrectly setup tests that expected `Result` to be endless.

    - Finalize "forward-only" behavior in `Result`, makes rewind a noop.
      Tests updated accordingly.

    - Change `Result`, to eager load first result.

    - Fix `Result` to correctly behave when iterating over it (next
      doesn't get called before first iteration). Update all tests
      and parts that relied upon the incorrect behavior.

    - Refactor `RecordSet` and `Result`.

    - Cleanup and documentation.

    - Adding test to prove non-sequential record issue.

    - Updating has many tests to explictly use main key order.