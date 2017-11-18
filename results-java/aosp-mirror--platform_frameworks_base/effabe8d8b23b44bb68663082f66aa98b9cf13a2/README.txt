commit effabe8d8b23b44bb68663082f66aa98b9cf13a2
Author: John Reck <jreck@google.com>
Date:   Thu Oct 27 09:09:36 2011 -0700

    Keep text handles in sync with native touch targets

     Bug: 5522153
     Also add some slop to the handle targets, cleaned
     up the JNI and improved debugging.

    Change-Id: I9ac637d793f2bd88d5315aa1483a4513d2496716