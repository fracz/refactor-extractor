commit 3cdeec5ed0ce25f3af3c563e109093b76383514c
Author: sgiehl <stefan@piwik.org>
Date:   Thu Jan 23 16:42:19 2014 +0100

    improved methods to get browser/os logos. always try to find an accurate logo, use family logo if no specific logo is found. never return unexisting path, return transparent logo instead