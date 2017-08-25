commit b682f335d3476443450bf970ba793b4e1009c3e2
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Sat Jun 24 20:23:45 2017 +0200

    ArrayDeclarationSpacing sniff: improve associative array detection

    A single-line non-associative array may contain a nested associative array.

    In that case, the non-associative array should *not* trigger an error, nor be auto-fixed, while the nested associative array should.

    Previously the non-associative array would also be fixed in that case.

    This commit adds the logic to distinguish between the two.