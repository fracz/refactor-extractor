commit f50305895d5c52df36ad11a959d5e5950c945db7
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Thu Nov 13 16:14:44 2008 +0000

    Began porting the QR Code encoder from ChartServer to Java/ZXing. Some important comments are flagged with JAVAPORT. I've done the following so far:

    - Created Java files with our headers, packages, etc.
    - Converted includes to imports, or commented out the unresolved dependencies
    - Merged all the .h and .cpp contents into Java classes
    - Fixed most of the formatting
    - Did all the simple transformations (bool, NULL, const, struct, string, ::, ->)
    - Created a Debug class to handle all the asserts and logging
    - Fixed about half of the static arrays
    - Removed some pthread cruft

    IMPORTANT:

    - Please do not start hacking this code up as I'm going to keep making large changes to it. In particular, we need to leave the trailing underscores on member variables for now. Once everything is compiling with no errors, we can revisit stylistic issues.
    - There will be a number of similar classes within the encoder and in the rest of ZXing. We should refactor those later (BitVector and BitArray come to mind). In the mean time, I want to get everything working and the tests passing before we do that.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@694 59b500cc-1b3d-0410-9834-0bbf25fbcc57