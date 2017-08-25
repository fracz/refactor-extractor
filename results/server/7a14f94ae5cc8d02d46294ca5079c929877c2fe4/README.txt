commit 7a14f94ae5cc8d02d46294ca5079c929877c2fe4
Author: voxsim <Simon Vocella>
Date:   Thu Sep 18 17:50:19 2014 +0200

    1. remove sizeof($filteredUsers) > 0 as condition
    2. use count instead of sizeof. Latter is an alias to first one, practically we stick to count everywhere. Having it consistent helps with readability.
    3. move whitespace so we have $groupUsers[] = $filteredUser; instead of $groupUsers []= $filteredUser;