commit 97114af8aeb33d0a9e4fb98685f003762a4b209e
Merge: 4e53165 98446e7
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jan 26 15:42:35 2016 +0100

    Merge pull request #16014 from cbuescher/refactor/rescore-fromXContent

    RescoreBuilder: Add parsing and creating of RescoreSearchContext

    Adding the ability to parse from xContent to the rescore builder. Also making RescoreBuilder an abstract base class that encapsulates the window_size setting, with QueryRescoreBuilder as its only implementation at the moment.

    Relates to #15559