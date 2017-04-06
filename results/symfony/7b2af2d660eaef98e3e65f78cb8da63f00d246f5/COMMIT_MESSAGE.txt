commit 7b2af2d660eaef98e3e65f78cb8da63f00d246f5
Merge: cef1915 ae99aa8
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 29 07:10:15 2016 +0200

    bug #19204 [Security] Allow LDAP loadUser override (tucksaun)

    This PR was merged into the 3.1 branch.

    Discussion
    ----------

    [Security] Allow LDAP loadUser override

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.1
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Back to 3.0, one could extend `Symfony\Component\Security\Core\User\LdapUserProvider` and override how User objects are created.
    Among several improvements, #17560 changed `loadUser` signature but also visibility to `private` which disallow any overriding.
    Even if the signature BC break is legitimate, we should still be able to override this method IMHO, which is not possible with a private visibility.
    This PRs introduces a `protected` visibility to allow again overriding.

    Commits
    -------

    ae99aa8 [Security] Allow LDAP loadUser override