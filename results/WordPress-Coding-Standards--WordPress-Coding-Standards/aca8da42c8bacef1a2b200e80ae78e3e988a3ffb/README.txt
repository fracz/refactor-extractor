commit aca8da42c8bacef1a2b200e80ae78e3e988a3ffb
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Thu Jul 13 08:15:22 2017 +0200

    :bug: ArrayIndentation sniff: various improvements to the reporting & fixing

    The `WordPress.Arrays.ArrayIndentation` sniff did not account for multi-line array items.
    Up to now, the first line would be handled, the subsequent lines, however, were disregarded.
    This has now been fixed.

    The fix takes into account that subsequent lines may have varying indentation, for instance to indicate HTML layout.
     > Subsequent lines may be indented more or less than the mimimum expected indent,  but the "first line after" should be indented - at least - as much as the very first line of the array item.
    Indentation correction for subsequent lines will be based on that diff.
    (inline comment in sniff)

    Fixes 973

    ------

    While working on the above, I realized that heredoc/nowdoc tokens - except for the opener - should be disregarded from indentation fixes by this sniff as it could break the closer and distort the text layout in a heredoc/nowdoc.
    The same goes for inline HTML, even thought that token should never be encountered in an array item.

    This has now been sorted as well.

    ------

    Lastly, arrays interspersed with comments, both for single line items as well as multi-line items, were not being aligned properly.

    This has also been fixed.

    Fixes 985

    ------

    Additional notes:
    * Includes an extensive range of additional unit tests.
    * I've added a public `$tabIndent` property to allow for this sniff to behave in a similar manner as the `Generic.WhiteSpace.ScopeIndent` sniff with regards to space vs tab indents. This will allow also for - at some point - potentially upstreaming the sniff to PHPCS itself. The space indent variation is unit tested in a second unit test case file using the same cases. To that end, the first unit test file has been renamed to `ArrayIndentationUnitTest.1.inc` (was `ArrayIndentationUnitTest.inc`).
    * I've renamed a number of internally used variables to make it easier to distinguish between them.
    * I've also split off parts of the code into separate methods to prevent/reduce code duplication.
        The `get_indentation_size()` and `get_indentation_string()` methods - and possibly even the `fix_alignment_error()` method - can be regarded as utility methods and could/should in the future be moved to the `WordPress_Sniff` class if & when other sniffs would need them.