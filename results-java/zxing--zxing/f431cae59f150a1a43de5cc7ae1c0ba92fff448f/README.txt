commit f431cae59f150a1a43de5cc7ae1c0ba92fff448f
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Wed Jun 11 00:05:51 2008 +0000

    - Added support for rotation in our blackbox test framework, and refactored the ways tests are created and run.
    - Turned on 0 and 180 degree rotation for all 1D formats.
    - Turned on 0, 90, 180, and 270 degree rotation for QR.
    - Changed the 1D code to re-enable upside down scanning, with a dramatic improvement in barcodes found and fewer false positives.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@411 59b500cc-1b3d-0410-9834-0bbf25fbcc57