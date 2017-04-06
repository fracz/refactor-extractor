commit a3dc8239832b3f8ea5aa76b044e0fc89cc0973c8
Merge: ce8d371 3d79d8b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Mar 21 18:50:37 2015 +0100

    feature #14001 [2.7] [Security] [ACL] Improved MaskBuilder and PermissionMap (AlexDpy)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [2.7] [Security] [ACL] Improved MaskBuilder and PermissionMap

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |  #13566
    | License       | MIT
    | Doc PR        | I'll do it if needed

    This PR refers to #13697 . The previous PR introduced a BC break, so i provide this one which is backward compatible.

    The MaskBuilderRetrievalInterface::getMaskBuilder methods allows us to retrieve a new instance of the MaskBuilder used in our application, even if it's a custom one.
    I also added a MaskBuilderInterface and an AbstractMaskBuilder which can be a great helper.

    Commits
    -------

    3d79d8b added MaskBuilderRetrievalInterface
    89a1f2a improved MaskBuilder