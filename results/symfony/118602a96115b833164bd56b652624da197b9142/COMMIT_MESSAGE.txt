commit 118602a96115b833164bd56b652624da197b9142
Merge: 2e74341 c2aeeeb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Feb 5 07:23:01 2015 +0100

    minor #13553 [2.7] [Form] Replaced calls to array_search() by in_array() (phansys)

    This PR was submitted for the 2.7 branch but it was merged into the 2.3 branch instead (closes #13553).

    Discussion
    ----------

    [2.7] [Form] Replaced calls to array_search() by in_array()

    [2.7] [Form] Replaced calls to ```array_search()``` by ```in_array()``` where is no need to get the index.

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | none
    | License       | MIT
    | Doc PR        | none

    It's a semantic improvement mostly, for readability (no performance impact).

    Commits
    -------

    c2aeeeb [2.7] [Form] Replaced calls to array_search() by in_array() where is no need to get the index