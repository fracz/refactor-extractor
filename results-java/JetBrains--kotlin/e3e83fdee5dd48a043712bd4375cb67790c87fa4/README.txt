commit e3e83fdee5dd48a043712bd4375cb67790c87fa4
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Fri Jul 11 18:50:13 2014 +0400

    Rework JetSourceFilterScope

    JetSourceFilterScope doesn't have to check file type
    Avoid redundant wrapping in JetSourceFilterScope (this may improve performance in case of frequent queries to indices)

    We can safely use kotlinSourcesAndLibraries scope in declaration provider given we have correct scope in the first place
    This allows for resolving kotlin js libraries (which are kotlin files in classes root) whithout additional hacks (i.e. IDEAConfig)