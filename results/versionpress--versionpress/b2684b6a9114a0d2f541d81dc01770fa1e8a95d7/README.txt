commit b2684b6a9114a0d2f541d81dc01770fa1e8a95d7
Author: Borek Bernard <borekb@gmail.com>
Date:   Mon Nov 24 01:53:07 2014 +0100

    [#175] Fixed bug introduced by refactoring in 4ca317e663aa23191cb307d0c668a8d36382c176 - Mirror was not properly reporting wasAffected(). Fixed by removing this property - Committer now checks the length of $mirror->getChangeList() instead (wasAffected() was not used anywhere else)