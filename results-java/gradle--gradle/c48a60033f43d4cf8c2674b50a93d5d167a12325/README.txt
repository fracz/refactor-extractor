commit c48a60033f43d4cf8c2674b50a93d5d167a12325
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Mar 17 13:15:12 2011 +0100

    (GRADLE-1339) Added basic fix for the issue. Now the tooling api honors eclipse's projectName customizations and projectName deduplication logic. Details:

    - EclipsePlugin now exposes the eclipseProject task. This is just a first step. In future it will expose the proper model object however it requires lots of refactoring.
    - Added integration test for new features (tooling api handles deduplication & project name customizations)
    - Changed slightly ToolingApiEclipseIntegrationTest.groovy so that it doesn't interfere with newly added project name deduplication
    - Tooling api's ModelBuilder now applies the eclipse plugin for all projects and runs eclipseConfigurer
    - Added necessary dependency from tooling-api to eclipse plugin