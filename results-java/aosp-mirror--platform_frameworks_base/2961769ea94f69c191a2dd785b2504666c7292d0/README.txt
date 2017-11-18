commit 2961769ea94f69c191a2dd785b2504666c7292d0
Author: Svetoslav <svetoslavganov@google.com>
Date:   Thu Apr 24 18:40:42 2014 -0700

    Adding APIs to render PDF documents.

    We need to render PDF documents for two main use cases. First,
    for print preview. Second, for resterizing the PDF document by
    a print service before passing it to a printer which does not
    natively support PDF (most consumer ones).

    Adding PDF rendering APIs improves guarantees for print quality
    as the same library is used for preview and rasterization. Also
    print vendors do not have to license third-party rendering engines.
    Last but not least as the platform uses PDF as its main print
    format it should also be able to natively render it.

    Change-Id: I57004a435db147663cafea40cf3296465aba7f99