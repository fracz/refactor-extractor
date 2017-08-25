commit 41624b31db8cad77ed1ee250631162613a1ca22b
Author: Chris Smith <chris.eureka@jalakai.co.uk>
Date:   Wed Mar 12 01:02:48 2008 +0100

    Update handler to merge consecutive 'cdata' instructions (incl. test case updates)

    This patch is the second in the series designed to make it easier for DW to allow
    plugins to modify the standard handling of line-breaks.

    Like the first this patch doesn't alter line-break behaviour at all, but introduces
    improvements that reduce to a minimum the number of 'cdata' instructions generated
    by the handler.

    darcs-hash:20080312000248-f07c6-f6ce1b5aac43a52cbe31215c517b048679ae20a7.gz