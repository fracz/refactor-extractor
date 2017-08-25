commit ebf0598e21e4aeea469df072192f31a8b9a3a9e2
Author: David Mudrák <david@moodle.com>
Date:   Wed Jul 27 16:20:18 2016 +0200

    MDL-55360 workshop: Allow creating workshops with empty grades to pass

    As a regression of MDL-55360, it was not possible to create new
    workshops if the field "Submission grade to pass" or "Assessment grade
    to pass" was left empty. The validation failed with "You must enter a
    number here".

    The fields submissiongradepass and gradinggradepass are always present
    as we explicitly define them in the mod form. So the isset() condition
    in the validation was useless and it did not allow to submit the form
    with these fields empty.

    Additionally, the unformat_float() returns null only if it receives
    empty value on the input. Which can't happen in our case so I am
    removing this condition to improve the readability of the code.