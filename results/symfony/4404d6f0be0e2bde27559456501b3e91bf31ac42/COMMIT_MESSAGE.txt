commit 4404d6f0be0e2bde27559456501b3e91bf31ac42
Merge: afa683a 373ab4c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 22 17:59:27 2011 +0100

    merged branch stof/entity_provider_registry (PR #2928)

    Commits
    -------

    373ab4c Fixed tests added from 2.0
    9653be6 Moved the EntityFactory to the bridge
    caa105f Removed useless use statement
    24319bb [DoctrineBridge] Made it possible to change the manager used by the provider

    Discussion
    ----------

    [DoctrineBridge] Made it possible to change the manager used by the provider

    This improves the support of several entity managers by allowing using a non-default one for the provider.

    It is BC for the user as the default value for the name is ``null`` which means using the default one.

    I'm preparing the PR for DoctrineBundle too

    ---------------------------------------------------------------------------

    by stof at 2011/12/19 14:16:38 -0800

    I'm wondering if the EntityFactory used to integrate the bundles with SecurityBundle should be moved to the bridge or not. Moving it (making the key and the abstract service id configurable) would allow reusing it in all Doctrine bundles instead of copy-pasting it (see the CouchDBBundle pull request linked above).
    The bridge was initially meant to integrate third party libraries with the components and this class is about the SecurityBundle, not the component. But on the other hand, we already share the abstract DI extension between the bundles using the bridge.

    ---------------------------------------------------------------------------

    by stof at 2011/12/19 14:17:48 -0800

    @fabpot @beberlei thoughts ?

    ---------------------------------------------------------------------------

    by stof at 2011/12/21 04:43:50 -0800

    @fabpot @beberlei what do you thing about moving the EntityFactory to the bridge ?

    ---------------------------------------------------------------------------

    by henrikbjorn at 2011/12/21 05:10:56 -0800

    Missing mongodb bundle

    ---------------------------------------------------------------------------

    by stof at 2011/12/21 05:52:06 -0800

    @henrikbjorn I was planning to send the PR for mongodb too but the namespace change was not merged yet yesterday. And now, you want to wait for the answer to know if I need to copy-paste the factory to the mongodb bundle too or if I move it to the bridge

    ---------------------------------------------------------------------------

    by beberlei at 2011/12/21 15:14:17 -0800

    I think moving it to the Bridge makes sense if we can re-use across all the bundles then. Also it is really about integrating security with doctrine, so its a bridge topic.

    ---------------------------------------------------------------------------

    by stof at 2011/12/22 08:39:52 -0800

    I updated the PR to move the factory to the bridge. The DoctrineBundle and DoctrineCouchDBBundle PRs are updated too.

    @fabpot the PR should be ready to be merged

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/22 08:53:02 -0800

    Tests do not pass for me:

        ...E

        Time: 0 seconds, Memory: 14.75Mb

        There was 1 error:

        1) Symfony\Tests\Bridge\Doctrine\Security\User\EntityUserProviderTest::testSupportProxy
        Argument 1 passed to Symfony\Bridge\Doctrine\Security\User\EntityUserProvider::__construct() must implement interface Doctrine\Common\Persistence\ManagerRegistry, instance of Doctrine\ORM\EntityManager given, called in tests/Symfony/Tests/Bridge/Doctrine/Security/User/EntityUserProviderTest.php on line 89 and defined

        src/Symfony/Bridge/Doctrine/Security/User/EntityUserProvider.php:35
        tests/Symfony/Tests/Bridge/Doctrine/Security/User/EntityUserProviderTest.php:89

    ---------------------------------------------------------------------------

    by stof at 2011/12/22 08:56:33 -0800

    @fabpot I fixed it before your comment (thanks travis ^^). It was the test added in my other PR to 2.0 and so not updated in the original commit. I forgot it when rebasing