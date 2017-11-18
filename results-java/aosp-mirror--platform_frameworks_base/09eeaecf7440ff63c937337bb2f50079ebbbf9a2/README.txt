commit 09eeaecf7440ff63c937337bb2f50079ebbbf9a2
Author: Victoria Lease <violets@google.com>
Date:   Tue Feb 5 11:34:13 2013 -0800

    refactor isAllowedBySettingsLocked()

    This commit splits LocationManagerService.isAllowedBySettingsLocked()
    into isAllowedByUserSettingsRogkei(), which takes a UID argument, and
    isAllowedByCurrentUserSettingsLocked(), which does not. This removes
    the need to generate synthetic UIDs with arbitrary application IDs
    and makes more explicit when LocationManagerService is acting on
    behalf of a caller and when it is acting on behalf of the device's
    current active user.

    Change-Id: I2cb8fb52687d2629848e5a4b66a4bda8f0f66fe1