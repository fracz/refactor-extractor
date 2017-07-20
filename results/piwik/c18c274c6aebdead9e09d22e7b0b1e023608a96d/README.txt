commit c18c274c6aebdead9e09d22e7b0b1e023608a96d
Author: mattab <matthieu.aubry@gmail.com>
Date:   Mon Jun 2 15:19:37 2014 +1200

    Small refactor of the Login/Auth object.

    Refs https://github.com/piwik/piwik/pull/299 - instead of creating loginFlow object, maybe we can throw events from the methods `processFailedSession()` and `processSuccessfullSession()`