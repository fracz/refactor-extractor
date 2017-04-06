commit 52153bd4541f374d433463ef11be0b6298595ae2
Author: Sam Brannen <sam@sambrannen.com>
Date:   Tue May 12 21:06:27 2015 +0200

    Document search scope in Ann*[Element]Utils

    This commit improves the documentation for AnnotationUtils and
    AnnotatedElementUtils by explaining that the scope of most annotation
    searches is limited to finding the first such annotation, resulting in
    additional such annotations being silently ignored.

    Issue: SPR-13015