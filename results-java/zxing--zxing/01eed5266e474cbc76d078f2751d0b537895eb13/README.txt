commit 01eed5266e474cbc76d078f2751d0b537895eb13
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Tue Jun 30 21:55:19 2009 +0000

    - Converted parts of the PDF 417 reader to use BitMatrix.
    - Changed the PDF 417 detector to search the image more broadly, instead of assuming the barcode starts in the left 1/4 and ends in the right 1/4. We now get 8/23 on the real-world blackbox test, and we'll probably do even better with further improvements here.
    - Added a quiet zone to two of the unit test images and updated a golden result for one of them. We now get 3/7 on the first test.
    - Tried to fix some bugs in the upside down code but it still doesn't work.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@1010 59b500cc-1b3d-0410-9834-0bbf25fbcc57