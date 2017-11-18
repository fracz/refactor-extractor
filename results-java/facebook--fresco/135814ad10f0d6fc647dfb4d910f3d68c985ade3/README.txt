commit 135814ad10f0d6fc647dfb4d910f3d68c985ade3
Author: Massimo Carli <maxcarli@fb.com>
Date:   Tue Mar 15 04:14:34 2016 -0700

    Fix gradle build after animation refactoring

    Summary:Fixed Gradle removing a dependency into a test
    Depends on D3023854

     This diff fixes the build with Gradle. Each modules can be built separately now.

    Reviewed By: kirwan

    Differential Revision: D3035606

    fb-gh-sync-id: 715a1ff87fb74257e2f47dcf9acba7f95f709eed
    fbshipit-source-id: 715a1ff87fb74257e2f47dcf9acba7f95f709eed