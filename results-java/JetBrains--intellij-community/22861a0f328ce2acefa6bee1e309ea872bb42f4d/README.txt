commit 22861a0f328ce2acefa6bee1e309ea872bb42f4d
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Fri Aug 2 21:13:16 2013 +0400

    IDEA-111342 Gradle: if root project is renamed by means of gradle, it is shown twice in tool window - as root project and as sub-project

    1. Correctly update cached data at ExternalSystemFacadeManager on project rename;
    2. Minor refactoring (method rename);
    3. Drop obsolete data from 'local external system settings' on project rename;
    4. Update external tool window project node representation on project rename;