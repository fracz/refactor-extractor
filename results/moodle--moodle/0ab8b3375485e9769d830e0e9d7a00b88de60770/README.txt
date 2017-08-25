commit 0ab8b3375485e9769d830e0e9d7a00b88de60770
Author: Simey Lameze <simey@moodle.com>
Date:   Wed Oct 26 09:34:44 2016 +0800

    MDL-29110 enrol_self: improve send course welcome message setting

    This commit change send course welcome message to a drop-down that now supports sending emails from:
    - Course contact
    - Enrolment key holder
    - No reply address
    Also moves part of the logic of handling send welcome email from to a new method get_welcome_email_contact() and unit test for this new method.