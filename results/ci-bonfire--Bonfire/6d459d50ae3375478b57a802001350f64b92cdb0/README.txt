commit 6d459d50ae3375478b57a802001350f64b92cdb0
Author: Alan Jenkins <alan.christopher.jenkins@gmail.com>
Date:   Fri Nov 23 13:15:31 2012 +0000

    admin theme: remove unused ajax loader (which has missing alt text)

    Unused code suffers from bitrot.  (E.g. who knows if mobile__header.php
    should include the loader?)

    And the loader creates warnings on every page, about the image with no alt
    text.  (That's not using any a11y tool; just the HTML5 validator).

    Setting alt text might be an improvement, but there's more to providing
    a11y than that when DOM manipulation is involved.  In that sense, the
    current non-ajax admin interface is rather easier to reason about than the
    old one :).