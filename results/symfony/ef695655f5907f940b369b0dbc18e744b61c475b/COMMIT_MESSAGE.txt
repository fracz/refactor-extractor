commit ef695655f5907f940b369b0dbc18e744b61c475b
Merge: 6fcb060 b2550b9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Oct 28 14:03:08 2013 +0100

    bug #9391 [Serializer] Fixed the error handling when decoding invalid XML to avoid a Warning (stof)

    This PR was merged into the 2.2 branch.

    Discussion
    ----------

    [Serializer] Fixed the error handling when decoding invalid XML to avoid a Warning

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    Without the exception being thrown here, the call to ``simplexml_import_dom`` throws a warning saying ``Invalid Nodetype to import``.
    This has been reported by @Tobion after we refactored FOSRestBundle to use this XmlEncoder instead of a duplication of a previous version of the class: https://github.com/FriendsOfSymfony/FOSRestBundle/pull/583/files#r7221045

    Commits
    -------

    b2550b9 Fixed the error handling when decoding invalid XML to avoid a Warning