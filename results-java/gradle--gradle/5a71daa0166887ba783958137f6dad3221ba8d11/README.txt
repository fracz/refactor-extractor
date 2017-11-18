commit 5a71daa0166887ba783958137f6dad3221ba8d11
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Aug 29 11:10:35 2011 +0200

    Stabilized the daemon functional test a bit. This test is still a bit flaky due to occasional OverlappingFileLockException but will do for now. I'll fix the file cache access with the single registry file refactoring.