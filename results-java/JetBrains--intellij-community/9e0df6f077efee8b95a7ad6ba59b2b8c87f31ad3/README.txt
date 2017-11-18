commit 9e0df6f077efee8b95a7ad6ba59b2b8c87f31ad3
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Tue Oct 12 15:35:54 2010 +0400

    Local changes refresh for IDEA investigations/improvements: repository location caching corrected to cache for VCS root, and ignored on IDEA level files are pre-removed from dirty scope, so VCS query wouldn't be run for ignored file