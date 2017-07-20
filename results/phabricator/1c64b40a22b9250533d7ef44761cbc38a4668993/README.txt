commit 1c64b40a22b9250533d7ef44761cbc38a4668993
Author: epriestley <git@epriestley.com>
Date:   Thu Jan 31 11:49:52 2013 -0800

    Minor improvements to Applications application

    Summary:
    I missed these in review, but here are a couple of tweaks:

      - Call `setWorkflow(true)` on the actions. This makes the dialogs pop up on the same page with Javascript if it's available.
      - When the user installs/uninstalls an application, send them back to the application's detail page, not the application list.

    Test Plan:
      - Uninstalled an application (saw dialog, got sent back to detail page).
      - Installed an application (saw dialog, got sent back to detail page).
      - Canceled an application uninstall.

    Reviewers: Afaque_Hussain

    Reviewed By: Afaque_Hussain

    CC: aran

    Differential Revision: https://secure.phabricator.com/D4762