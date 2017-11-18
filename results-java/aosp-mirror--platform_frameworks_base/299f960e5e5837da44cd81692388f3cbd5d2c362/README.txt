commit 299f960e5e5837da44cd81692388f3cbd5d2c362
Author: Craig Mautner <cmautner@google.com>
Date:   Mon Jan 26 09:47:33 2015 -0800

    Add reason string for bringing stack to front

    Additional debug and useful information.
    Also removed am_resume_activity verbosity and refactored method to
    eliminate unused parameter.

    For bug 17721767.

    Change-Id: Ie1c0652a38a0c6ae6db27a52a9e5da29e252e300