commit fbc9082e813c6cef79b391bf27b379a106b66c13
Merge: 6d68740 21218cc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Aug 2 15:35:53 2013 +0200

    merged branch StanAngeloff/issue-8424-refactoring (PR #8514)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Serializer] Added XML attributes support in XmlEncoder

    This is a rebase and refactoring of #8424.

    ---

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8424
    | License       | MIT
    | Doc PR        | -

    ---

    | New Code | Result
    | --- | ---
    | Code coverage | 100%
    | PSR-2 | No violations
    | PHP-CS-Fixer | No changes

    ---

    ### TODO

    - [ ] **Q**: I looked through `symfony-docs` for any mention of `xml_root_node_name` which is already implemented, but failed to find any. How to best document those new additions?

    Commits
    -------

    21218cc [Serializer] Added XML attributes support for DomDocument in XmlEncoder.