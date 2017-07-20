commit b23c85b16915d94cebf37b9cefdc981702ae3f8c
Author: Chad Little <chad@phacility.com>
Date:   Tue May 31 12:15:23 2016 -0700

    Add role=dialog to all dialogs

    Summary: Seen some complaints about usability here, adding role=dialog to improve when these trigger.

    Test Plan: Turn on Voiceover, tab over to log out link, here proper dialog title and text before highlighted submit button.

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin

    Differential Revision: https://secure.phabricator.com/D15993