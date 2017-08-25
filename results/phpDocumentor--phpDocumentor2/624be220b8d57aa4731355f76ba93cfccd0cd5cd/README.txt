commit 624be220b8d57aa4731355f76ba93cfccd0cd5cd
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu May 17 16:31:05 2012 +0200

    Refactored file handling into a separate Component structure

    The previous was file handling was an improvement over the one
    before but we want to extract the Fileset component out of
    phpDocumentor. Another added benefit was that performance is
    improved by re-using Symfony's Finder component.

    After this commit must the code be moved to it's own repository
    and included via Composer.