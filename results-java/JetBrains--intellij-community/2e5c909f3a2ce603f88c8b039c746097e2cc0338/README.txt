commit 2e5c909f3a2ce603f88c8b039c746097e2cc0338
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Wed Oct 12 21:02:16 2016 +0300

    [vcs]: use per Repository ignored holders in VcsIgnored; add get size methods

    * (IDEA-113185, IDEA-137014, IDEA-138132, IDEA-159684) fix performance;
    * provide dir and file sizes method for Ignored files;
    * improve ignored rendering with "updating" word;
    * do not ask for ignored if they shouldn't be shown (to avoid map union);
    * remove VF set from HgIgnoredFilesHolder, it was already moved to
     Composite holder and used as IDE ignored files storage;