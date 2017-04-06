commit 1fb8f8817ad121643e2fb5bf1a90e0ea4e0f6aa8
Merge: 56a7a60 d3bafc6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Sep 23 11:51:23 2014 +0200

    feature #11183 [Security] add an AbstractVoter implementation (Inoryy)

    This PR was squashed before being merged into the 2.6-dev branch (closes #11183).

    Discussion
    ----------

    [Security] add an AbstractVoter implementation

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT
    | Doc PR        | https://github.com/symfony/symfony-docs/pull/4257

    The idea is to reduce boilerplate required to create custom Voter, doing most of the work for the developer and guiding him on the path by providing simple requirements via abstract methods that will be called by the AbstractVoter.

    P.S. This is meant to be a [DX Initiative](https://github.com/symfony/symfony/issues?labels=DX&state=open) improvement.

    Commits
    -------

    d3bafc6 [Security] add an AbstractVoter implementation