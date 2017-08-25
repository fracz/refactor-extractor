commit 87b5c584fbd4911a55b40a57cae5a2a6f8576df7
Author: David Mudrák <david@moodle.com>
Date:   Mon Jul 25 19:40:26 2016 +0200

    MDL-55289 workshop: Fix files processing in example submissions

    While testing the issue MDL-55289 I realized that attaching files to
    workshop example submissions does not work at all and throws an error.

    The reason was that in MDL-50794 (996f7e82), the variables $contentopts
    and $attachmentopts were replaced with the result of the methods
    submission_content_options() and submission_attachment_options().
    But I forgot to perform the full refactoring in exsubmission.php too.

    Attached behat test should cover both issues on this branch.