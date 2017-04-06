commit 4b4098bfcae64f69c70a22393de1f3d9a0d3dc46
Author: Juan Gabriel Jimenez Campos <juangabreil@gmail.com>
Date:   Tue Oct 21 16:08:21 2014 +0200

    fix(select): assign result of track exp to element value

    Fixes a regression where the option/select values would always be set to
    the key or index of a value within the corresponding collection. Prior to
    some 1.3.0 refactoring, the result of the track expression would be bound
    to the value, but this behavior was not documented or explicitly tested. A
    cache was added in order to improve performance getting the associated
    value for a given track expression.

    This commit adds one explicit test for this behavior, and changes several
    other trackBy tests to reflect the desired behavior as well.

    Closes #9718
    Fixes #9592