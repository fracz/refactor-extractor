commit 35bed96ac7b1345a3c286f8c08763ecb0447ef38
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Mar 19 22:37:21 2013 +0100

    REVIEW-1714 Refactored the logic behind sorting the items of the profile report and added coverage. We could push further and more improvements, like sorting alphabetically when elapsed time is the same, using tree sets to avoid redundant creation of collections. For now what's included in the commit should be enough.