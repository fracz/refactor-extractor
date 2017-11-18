commit 5e9b2fed45834e13de24038175c9c270ad614989
Author: Lisa Luo <luoser@users.noreply.github.com>
Date:   Tue Nov 29 16:33:39 2016 -0500

    Remove deprecated view model methods

    * Update ActivityFeedViewModel inputs and outputs

    Remove deprecated view() subjects, organize stuff

    * move VM subscriptions from onCreate to constructor in prep for testing

    * add ActivityFactory; update ActivityEnvelope with Builder; update MockApiClient fetchActivities endpoint

    * refactor ActivityFeedVM outputs to be Observables

    * add ActivityFeedVM test

    * Revert Observable outputs to Behavior Subjects; move inputs and outputs to bottom of file

    * update Android Studio version

    * add LoginVM test, update TFA test, fix ApiExceptionFactory

    * refactor Login and ActivityFeed vms WIP

    * dialog behavior wip

    * fix dialog behavior when clicking outside of dialog to cancel

    * fix password reset dialog on rotation behavior

    * lil shuffling of inputs and outputs at end of vm files

    * remove deprecated view in ViewPledgeVM

    * fix activities emitting; add test; fix login tests

    * checkstyle

    * refactor outputs

    * remove remaining deprecated view model methods

    * remove unneeded ::onNexts

    * update LoginViewModel test with proper dialog behavior

    * initial cleanups

    static imports, renaming variables