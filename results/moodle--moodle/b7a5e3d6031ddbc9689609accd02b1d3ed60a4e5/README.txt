commit b7a5e3d6031ddbc9689609accd02b1d3ed60a4e5
Author: David Mudrak <david@moodle.com>
Date:   Thu Apr 26 14:50:57 2012 +0200

    MDL-32638 improved file browser access to submission_content and submission_attachment areas

    The user has to have viewallsubmissions capability to be able to see the
    submission files in the browser. Additionally, in the separate groups
    mode, the user has to have accessallgroups or share at least one group
    with the submission author to view their files.