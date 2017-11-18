commit 5fd92ede16309d144a8456cdfdb418a065d3baf6
Author: Adam Murdoch <a@rubygrapefruit.net>
Date:   Fri Sep 26 23:39:56 2008 +0000

    GRADLE-163:
    - Some initial refactoring, to move some of the 'data' about a build into the Build object, rather than passing it around as parameters or injecting it as a service.
    - Introduced BuildInternal, which extends Build.
    - ProjectsLoader is now responsible for creating the Build instance
    - Removed IProjectRegistry.reset(), IProjectFactory.reset() and ProjectsLoader.reset()

    git-svn-id: http://svn.codehaus.org/gradle/gradle-core/trunk@891 004c2c75-fc45-0410-b1a2-da8352e2331b