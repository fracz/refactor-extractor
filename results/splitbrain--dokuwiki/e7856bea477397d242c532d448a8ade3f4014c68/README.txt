commit e7856bea477397d242c532d448a8ade3f4014c68
Author: chris <chris@jalakai.co.uk>
Date:   Tue Feb 6 02:48:21 2007 +0100

    refactor renderer header() to separate out adding toc items

    adds a new render method toc_additem($id, $text, $level)

    This allows toc items to be added separately from the header() function, allowing
    plugins to generate their own table of content items without having to copy dw
    core code (which may in the future change).

    darcs-hash:20070206014821-9b6ab-218184e543f6b348e710acc2fe30a8ec329c66a8.gz