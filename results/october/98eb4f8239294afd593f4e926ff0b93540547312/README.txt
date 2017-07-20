commit 98eb4f8239294afd593f4e926ff0b93540547312
Author: Samuel Georges <sam@daftspunk.com>
Date:   Sat Feb 27 11:29:07 2016 +1100

    Add support for "relation" with Tree models
    - This makes the list slightly more efficient for small collections, less efficient for larger collections. If this becomes a problem in future we may need to look at a solution that grabs all the root nodes to start, then lazy loads any expanded nodes as secondary AJAX requests.
    - Write tests for the Tree trait improvements
    Fixes #1647