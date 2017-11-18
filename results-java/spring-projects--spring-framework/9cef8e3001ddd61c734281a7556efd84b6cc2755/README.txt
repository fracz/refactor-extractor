commit 9cef8e3001ddd61c734281a7556efd84b6cc2755
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Nov 11 07:12:44 2014 +0100

    Apply extra checks to static resource handling

    - remove leading '/' and control chars
    - improve url and relative path checks
    - account for URL encoding
    - add isResourceUnderLocation final verification

    Issue: SPR-12354