commit aa69c4a20b106079e9f67dfd1ecaa5a8e05f8ba7
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Nov 26 19:45:33 2015 +0100

    Add fromXContent method to HighlightBuilder

    For the search refactoring the HighlightBuilder needs a way to
    create new instances by parsing xContent. For bwc this PR start
    by moving over and slightly modifying the parsing from
    HighlighterParseElement and keeps parsing for top level highlighter
    and field options separate. Also adding tests for roundtrip
    of random builder (rendering it to xContent and parsing it and
    making sure the original builder properties are preserved)