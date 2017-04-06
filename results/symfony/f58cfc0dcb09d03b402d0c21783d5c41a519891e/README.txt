commit f58cfc0dcb09d03b402d0c21783d5c41a519891e
Merge: 4316595 649fa52
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 19 19:50:04 2011 +0100

    merged branch stof/entity_provider_proxy (PR #2922)

    Commits
    -------

    649fa52 [DoctrineBridge] Fixed the entity provider to support proxies
    29f4111 [DoctrineBridge] Added a failing test showing the issue for proxy users

    Discussion
    ----------

    Fixed the entity provider to support proxies

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: yes
    Symfony2 tests pass: yes

    If a proxy object was used, the ``supportsClass`` method would fail becasue it does a string comparison for the class name.

    This issue has not been reported by users yet because it is an edge case:

    - ``supportsClass`` is used only in the RememberMe system
    - getting a proxy in the entity provider is possible only if a listener running before the firewall loaded an object which has a relation to the user, which is far from being a standard use case.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/12/19 10:07:46 -0800

    How about using the new proxy tools that Doctrine has?

    ---------------------------------------------------------------------------

    by stof at 2011/12/19 10:20:37 -0800

    the new tool will only be available in 2.2 so only for Symfony 2.1. Once merged into master, we could eventually refactor it in the master branch to use ``Doctrine\Common\Util\ClassUtils``