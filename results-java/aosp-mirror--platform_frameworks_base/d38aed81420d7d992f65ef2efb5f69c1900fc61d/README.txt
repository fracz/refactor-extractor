commit d38aed81420d7d992f65ef2efb5f69c1900fc61d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Jun 10 21:36:35 2014 -0700

    Some tweaks to improve document task creation.

    - Mark the chooser activity as never launching in to a new
      task, even if the caller asks for it.  These are dialogs
      so don't make sense as stand-alone tasks.  (Maybe later
      the policy should be to not launch into a new task in any
      case that the activity is a dialog or even transparent at all.)

    - Keep track in the task record of whether any activities in
      it have been shown to the user, and use this to automatically
      remove the task when all activities finish.  This leans up
      cases where apps are launching stub activities that get turned
      in to tasks but are never seen by the user because they
      immediately launch another activity in another task and
      then finish.

    Change-Id: I00b641c80aa96bd5071479f36ee2e4d8e3a81aeb