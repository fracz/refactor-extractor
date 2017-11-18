commit f6073e9e4c966a530b13f0b2f852e5f09e9a2ef9
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Nov 11 14:45:57 2010 +0300

    IDEA-58070 Soft wrap: Improve soft wraps performance

    Performance improvement -> Arrays.fill() and further check for the stored value is switched to direct examination if the storage end offset is exceeded