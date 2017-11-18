commit 1da9bd60b33a95aa617110192f81e553f51e74a7
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Tue Jun 16 18:47:34 2009 +0000

    Added a new blackbox test with extreme shadows and highlights. We do pretty poorly on this, but I've been experimenting with local thresholding algorithms which are a night and day improvement.

    Also made the ImageConverter app ignore some kinds of files.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@969 59b500cc-1b3d-0410-9834-0bbf25fbcc57