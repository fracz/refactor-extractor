commit 8f488bd01962990f69ced2d68bfe04b629689f17
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Wed Nov 19 11:54:59 2014 +0100

    Update init to new metadata format

    Spring initializr now declares an improved metadata format (v2).

    InitializrServiceMetadata has been updated to parse this format. Note
    that the client could be further improved by using HAL generated links.

    Closes gh-1953