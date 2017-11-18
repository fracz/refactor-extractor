commit 7af486c6cd8df7dda9e5986e085d2a78eb878453
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Oct 26 19:42:42 2012 +0400

    [diff] Highlight the whole line for inline fragment, but with less intensive color.

    * Don't highlight the line marker renderer for inline changes - the line fragment wrapping them will be used.
    * Therefore remove the code for adjusting text range height from the DiffLineMarkerRenderer.
    * Introduce TextDiffType#myInlineWrapper for diff type of a fragment that is a wrapper for inline changes.
     - use special background color for such type.
     - refactor getTextAttributes: for scheme just return the color from scheme, all adjustments (for applied and for inline changes) are made in getTextAttributes(Editor).
    * Create special inline TextDiffType instance where needed: in the LineBlocks (for DividerPolygons) and in the DiffMarkup (for the gutter and code highlighting).