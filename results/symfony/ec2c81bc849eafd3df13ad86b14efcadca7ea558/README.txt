commit ec2c81bc849eafd3df13ad86b14efcadca7ea558
Merge: c98c2ef d2195cc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Nov 17 16:00:33 2011 +0100

    merged branch stof/security_providers (PR #2454)

    Commits
    -------

    d2195cc Fixed phpdoc and updated the changelog
    9e41ff4 [SecurityBundle] Added a validation rule
    b107a3f [SecurityBundle] Refactored the configuration
    633f0e9 [DoctrineBundle] Moved the entity provider service to DoctrineBundle
    74732dc [SecurityBundle] Added a way to extend the providers section of the config

    Discussion
    ----------

    [WIP][SecurityBundle] Added a way to extend the providers section of the config

    Bug fix: no
    Feature addition: yes
    BC break: <del>no (for now)</del> yes
    Tests pass: yes

    This adds a way to extend the ``providers`` section of the security config so that other bundles can hook their stuff into it. An example is available in DoctrineBundle which is now responsible to handle the entity provider (<del>needs some cleanup as the service definition is still in SecurityBundle currently</del>). This will allow PropelBundle to provide a ``propel:`` provider for instance.

    In order to keep BC with the existing configuration for the in-memory and the chain providers, I had to allow using a prototyped node instead of forcing using an array node with childrens. This introduces some issues:

    - impossible to validate easily that a provider uses only one setup as prototyped node always have a default value (the empty array)
    - the ``getFixableKey`` method is needed in the interface to support the XML format by pluralizing the name.

    Here is my non-BC proposal for the configuration to clean this:

    ```yaml
    security:
        providers:
            first:
                memory: # BC break here by adding a level before the users
                    users:
                         joe: { password: foobar, roles: ROLE_USER }
                         john: { password: foobarbaz, roles: ROLE_USER }
            second:
                entity: # this one is BC
                    class: Acme\DemoBundle\Entity\User
            third:
                id: my_custom_provider # also BC
            fourth:
                chain: # BC break by adding a level before the providers
                     providers: [first, second, third]
    ```

    What do you think about it ? Do we need to keep the BC in the config of the bundle or no ?

    Btw note that the way to register the factories used by the firewall section should be refactored using the new way to provide extension points in the extensions (as done here) instead of relying on the end user to register factories, which would probably mean a BC break anyway.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/10/23 09:19:23 -0700

    i don't think we should keep BC. the security config is complex as is .. having BC stuff in there will just make it even harder and confusing.

    ---------------------------------------------------------------------------

    by willdurand at 2011/10/23 09:41:25 -0700

    Is the security component tagged with `@api` ?

    So basically, we just have to create a factory (`ModelFactory` for instance) and to register it in the `security` extension, right ? Seems quite simple to extend and much better than the hardcoded version…

    Why did you call the method to pluralize a key `getFixableKey` ?

    ---------------------------------------------------------------------------

    by beberlei at 2011/10/23 14:48:26 -0700

    Changing security config will introduce risk for users. We should avoid that

    ---------------------------------------------------------------------------

    by stof at 2011/10/23 15:34:47 -0700

    @beberlei as the config is validated, it will simply give them an exception during the loading of the config if they don't update their config.

    ---------------------------------------------------------------------------

    by stof at 2011/10/24 01:01:42 -0700

    @schmittjoh @fabpot Could you give your mind about it ?

    ---------------------------------------------------------------------------

    by stof at 2011/10/31 17:08:12 -0700

    @fabpot @schmittjoh ping

    ---------------------------------------------------------------------------

    by stof at 2011/11/11 14:08:18 -0800

    I updated the PR by implementing my proposal as the latest IRC meeting agreed that we don't need to keep the BC for this change. This allows to add the validation rule now.

    ---------------------------------------------------------------------------

    by stof at 2011/11/16 11:16:06 -0800

    @fabpot ping

    ---------------------------------------------------------------------------

    by fabpot at 2011/11/16 22:29:05 -0800

    @stof: Before merging, you must also add information about how to upgrade in the CHANGELOG-2.1.md file.

    ---------------------------------------------------------------------------

    by stof at 2011/11/17 00:01:23 -0800

    @fabpot done