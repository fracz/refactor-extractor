commit 181f303d16c2076184325b14ceb694594b814e7f
Author: Todd Burry <todd@vanillaforums.com>
Date:   Wed Feb 11 14:06:42 2015 -0500

    Fix the Gdn_ConfigurationModel to work with the validation object refactor.

    - Support the Gdn_ConfigurationModel’s odd use of Gdn_Validation->SetSchema().
    - Remove the double reference in Gdn_ConfigurationModel::__construct().