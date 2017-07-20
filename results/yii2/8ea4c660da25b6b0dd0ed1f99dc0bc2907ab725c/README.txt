commit 8ea4c660da25b6b0dd0ed1f99dc0bc2907ab725c
Author: Sam Mousa <sam@mousa.nl>
Date:   Thu Feb 11 13:43:40 2016 +0000

    Several improvements to DI container

    - Refactored invoke into separate resolve method (reusability)
    - Added support for associative params array
    - Added PHPUnit group doc to DI tests.
    - Improved the tests for better coverage, and fixed a bug discovered by the new tests.

    close #10811