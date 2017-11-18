commit b61311261585f5b43fe510798837bbbcbd9fe9a4
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Mon Jun 22 21:03:34 2009 +0000

    Rewrote BitMatrix to allow rectangular 2D arrays, and so that every row begins with a new int, which makes it fast to copy out rows into BitArrays. This will be the basis of the upcoming bitmap refactoring for 1D Readers.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@978 59b500cc-1b3d-0410-9834-0bbf25fbcc57