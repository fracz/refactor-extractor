commit 9546be39746a62a2746be6af5d7d3e6827395730
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Fri May 4 18:58:47 2012 -0400

    Fix JavaDoc in OpMultiply so that it does not contain special characters

    The JavaDoc in OpMultiply contained special characters that caused
    problems when building with Java 7 on Mac OS X. The section symbol has
    been replaced with the word "Section". Also improved class-level and
    method-level JavaDoc in general.