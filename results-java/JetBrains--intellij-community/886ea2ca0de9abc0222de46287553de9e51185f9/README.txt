commit 886ea2ca0de9abc0222de46287553de9e51185f9
Author: Vladislav.Soroka <Vladislav.Soroka@jetbrains.com>
Date:   Mon Apr 14 16:59:00 2014 +0400

    Alex's fix about getting the physical path of a Gradle sub-project from a GradleBuild moved from Android plugin to Gradle plugin, see Change-Id: I2617fc929d18f23447d658ed2f4ae084b64847af and  Ifa59c4810615fa27f5f8d5753bc89ea85374ff39

    Regression bug caused by this change fixed - http://youtrack.jetbrains.com/issue/IDEA-123395

    Also string data used as file paths in IDEA project files should have normalized form (see ExternalSystemApiUtil#toCanonicalPath(). It will allow to be automatically converted into IDEA relative paths and be used in refactorings (e.g. c:/projects/a_project/apps/appA => $PROJECT_DIR$/apps/appA)