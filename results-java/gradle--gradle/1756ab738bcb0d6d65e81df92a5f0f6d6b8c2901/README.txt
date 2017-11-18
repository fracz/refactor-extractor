commit 1756ab738bcb0d6d65e81df92a5f0f6d6b8c2901
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Mar 19 12:38:48 2013 +0100

    Started adding coverage to the profile report. Decoupled BuildProfile from Gradle object. Added some tests around build run description. Added very basic handling of scenario when no tasks are specified. At some point it would be nice to improve the build description and include other useful information: daemon, parallel, configuration on demand, project properties, default tasks selected for build etc. The profile report can also include links to our guide where we discuss how to improve performance, typical pitfalls, etc.