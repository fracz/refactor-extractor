commit 62a4cc90cc8560b013fab8882ef48d13c0b58ed5
Merge: c9cfcc1 fcabadf
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jan 19 08:48:06 2013 +0100

    merged branch beberlei/SerializerOptions (PR #6797)

    This PR was merged into the master branch.

    Commits
    -------

    fcabadf Fix JsonDecode to work on PHP 5.3, update the CHANGELOG.md
    b6bdb45 Completly refactor the Serializer Options Pull Request to push context information directly and avoid state and dependencies between SerializerInterface and encoders/normalizers.
    ef652e2 Added context to JsonEncoder
    eacb7e2 Rename $options to $context, as it makes the intent much more clear.
    8854b85 Fix CS issues, removed global options
    9c54a4b [Serializer] Allow options to be passed to SerialiizerInterface#serialize and #unserialize. Thsee options are available to all encoders/decoders/normalizers that implement SerializerAwareInterface.

    Discussion
    ----------

    [2.2] [Serializer] Configurable Serializer

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: #4907, #4938
    License of the code: MIT
    Todo:

    This is an extension of GH-6574 that removes the context state in favor of passing this information around.

    ---------------------------------------------------------------------------

    by beberlei at 2013-01-18T13:12:39Z

    @fabpot @lsmith I think this is how it should work from an OOP/OOD perpesctive, avoiding the context state. This makes for a much cleaner code and dependency graph.

    ---------------------------------------------------------------------------

    by lsmith77 at 2013-01-18T14:14:37Z

    makes sense. anything fancier would lose this components simplicity which IMHO is the main benefit versus JMS serializer.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-18T14:26:25Z

    Looks very good. :+1:

    ---------------------------------------------------------------------------

    by beberlei at 2013-01-18T14:37:32Z

    I need to fix the failures with the JsonEncoder and then this is good to merge

    ---------------------------------------------------------------------------

    by stof at 2013-01-18T14:40:21Z

    you also need to update the CHANGELOG of the component

    ---------------------------------------------------------------------------

    by beberlei at 2013-01-18T23:17:57Z

    Fixed, only the Redis Profiler problem still failing the Travis builds. Also I updated the CHANGELOG.md.

    @fabpot  Good to merge from my POV

    ---------------------------------------------------------------------------

    by stof at 2013-01-18T23:27:59Z

    @beberlei see #6804 for the Redis profiler issue