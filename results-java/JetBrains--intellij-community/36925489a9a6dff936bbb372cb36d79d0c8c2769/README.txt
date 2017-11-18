commit 36925489a9a6dff936bbb372cb36d79d0c8c2769
Author: Maxim.Mossienko <Maxim.Mossienko@jetbrains.com>
Date:   Tue Apr 2 21:25:55 2013 +0200

    small refactoring for better code understanding and easier subsequent change
    - devirtualize ContentOutputStream and AttributeOutputStream by inlining BaseOutputStream to them
    - inline findContentPage into read and write usage
    ----------
    so far after refactoring becomes evident that synchronized by attributeId seems to be redundant