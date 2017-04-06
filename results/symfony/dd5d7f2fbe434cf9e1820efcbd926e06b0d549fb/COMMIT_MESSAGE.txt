commit dd5d7f2fbe434cf9e1820efcbd926e06b0d549fb
Merge: b47cb35 6483d88
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Apr 12 06:50:51 2012 +0200

    merged branch jalliot/acl_proxies-2 (PR #3826)

    Commits
    -------

    6483d88 [Security][ACL] Fixed ObjectIdentity::fromDomainObject and UserSecurityIdentity::from(Account|Token) when working with proxies

    Discussion
    ----------

    [WIP] Fixed ACL handling of Doctrine proxies

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/jalliot/symfony.png?branch=acl_proxies-2)](http://travis-ci.org/jalliot/symfony)
    Fixes the following tickets: #3818, #2611, #2056, #2048, #2035 and probably others
    Todo: Fix tests, update changelog

    Hi,

    As per @fabpot's request, here is #3818 ported to `master`.

    > Here is a new attempt to fix all the issues related to Symfony ACL identities and Doctrine proxies.
    > It only fixes the issue for Doctrine >=2.2 (older versions of Doctrine will still not work properly with ACL because `Doctrine\Common\Util\ClassUtils` didn't exist before and proxy naming strategy was not consistent between all Doctrine implementations (ORM/ODM/etc.)).

    /cc @schmittjoh @beberlei

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-09T00:07:52Z

    I'm -1 on adding a dependency on the Doctrine class.

    The naming scheme was designed in a generic way, we should just copy the method.

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-09T00:13:07Z

    @schmittjoh The ACL component requires Doctrine DBAL already (and as such Common) so I don't think this is really an issue at the moment. If (when?) the component is refactored to be decoupled from Doctrine, then maybe we will have to change that.
    But I can also copy the class (where?) if you think it is better :)

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-09T01:01:27Z

    I'd suggest ``Symfony\Component\Security\Core\Util\ClassUtils``.

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-09T11:16:19Z

    @fabpot @schmittjoh It's done: I've backported the ClassUtils class and its tests from Doctrine Common into the Security component. Maybe this PR can be merged into 2.0 as well now, what do you think?

    ---------------------------------------------------------------------------

    by oscherler at 2012-04-11T06:27:20Z

    There seems to be a consensus that ACLs don’t (fully?) work with Doctrine < 2.2, i.e. in Symfony 2.0. Can it therefore be documented somewhere, typically on the main documentation page on the subject? http://symfony.com/doc/current/cookbook/security/acl.html

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-11T07:36:25Z

    @oscherler You can make ACL work with Doctrine < 2.2 and without this patch if you use something like this code (note this is for ORM only but it can be adapted for ODM; it also assumes that your identifier can be retrieved with `getId()`):

    ``` php
    <?php

    use Doctrine\ORM\Proxy\Proxy;
    use Symfony\Component\Acl\Domain\ObjectIdentity;

    $domainObject = ... // some Doctrine entity (maybe a proxy...)

    if ($domainObject instanceof Proxy) {
        $objectIdentity = new ObjectIdentity($domainObject->getId(), get_parent_class($domainObject));
    } else {
        $objectIdentity = new ObjectIdentity($domainObject->getId(), get_class($domainObject));
    }
    ```
    It is ugly but it is the only way to get the correct identity for < 2.2. Never use `ObjectIdentity::fromDomainObject` with a proxy and without this patch!
    The same applies to `UserSecurityIdentity`.

    This should indeed be documented in the doc for 2.0. /cc @weaverryan

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-11T22:34:37Z

    I just fixed the tests and squashed the commits.
    I didn't write a test for `UserSecurityIdentity::fromAccount` and `fromToken` because I didn't really have time to look for a clean solution (without creating a full UserInterface implementation for instance...). Anyway the test for `ObjectIdentity::fromDomainObject` should be enough I guess...
    Don't hesitate to add one if you think it is necessary. /cc @schmittjoh

    @fabpot Apart from @beberlei approval to use MIT license, I think it is ready for merge.