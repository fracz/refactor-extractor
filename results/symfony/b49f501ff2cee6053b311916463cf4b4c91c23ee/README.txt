commit b49f501ff2cee6053b311916463cf4b4c91c23ee
Merge: e7b55fc 71db836
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 20 07:42:10 2012 +0200

    merged branch jeanmonod/config-with-numeric-checks (PR #4714)

    Commits
    -------

    71db836 Better config validation handling for numerical values:  * New node type Integer and Float  * New expressions: min() and max()

    Discussion
    ----------

    [2.2] [Config] Better handling for numerical values:

    * New node type Integer and Float
    * New expressions: ifLessThan(), ifGreaterThan(), ifInRange(), ifNotInRange()

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-03T08:50:22Z

    As I said on PR #4713, adding more method clutters the API without any big benefits. I'm -1 on the PR.

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T08:54:36Z

    I have been discuss it with @schmittjoh at the sflive, he was thinking it could be a good addition.
    IMHO I think that if we want to encourage the usage of bundle configuration validation, we should make it as easy as possible to use...

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T08:59:42Z

    A real life example:

        ->scalarNode('max_nb_items')
            ->validate()
                ->ifTrue(function($v){
                    return !is_int($v) || (is_int($v) && $v<1);
                })
                ->thenInvalid('Must be a positive integer')
            ->end()
        ->end()

    could be replaced by

        ->integerNode('max_nb_items')
            ->validate()
                ->ifLessThan(1);
                ->thenInvalid('Must be a positive integer')
            ->end()
        ->end()

    ---------------------------------------------------------------------------

    by gnutix at 2012-07-03T09:03:06Z

    I agree with @jeanmonod on this matter, the bundle configuration validation is already kind of a hassle to understand (and read), so it would be a good addition IMHO.

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-03T10:54:32Z

    @schmittjoh What's your point of view?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-03T14:10:37Z

    The integer and float nodes are valuable additions imo which I wanted to add myself several times but simply did not have the time for.

    As for the changes to the expression builder, I was not really passionate about them in Paris, but I did not mind either way. However, looking at this PR, I think they would be better implemented as methods on the definition builders, and validated directly by the nodes:

    ```php
    $builder->integerNode('foo')->range(1, 4)->end();
    $builder->integerNode('foo')->mustBeGreaterThan(5)->end();
    ```

    This will also allow for these constraints to be introspected and added to generated documentation.

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-03T17:57:25Z

    @jeanmonod Can you take into account the comments by @schmittjoh?

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T19:40:24Z

    @fabpot Yes, I will try to move those 4 checks.

    @schmittjoh If I put those tests into the ScalarNodeDefinition did you think it's ok? And did I have to make anything special for the documentation introspection?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-03T19:56:00Z

    You can take a look at the EnumNodeDefinition, and the EnumNode. They are
    pretty simple, and should give you a good idea of how to implement it.

    On Tue, Jul 3, 2012 at 9:46 PM, Jeanmonod David <
    reply@reply.github.com
    > wrote:

    > @fabpot Yes, I will try to move those 4 checks.
    >
    > @schmittjoh If I put those tests into the ScalarNodeDefinition did you
    > think it's ok? And did I have to make anything special for the
    > documentation introspection?
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/4714#issuecomment-6744309
    >

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T21:37:18Z

    OK, I just refactor as requested. At the end, I didn't add the range() check. It can be easily done by chaining min and max, like this:

        $builder->integerNode('foo')->min(1)->max(4)->end();

    @schmittjoh can you have a look?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-03T21:48:17Z

    Have you tested the builder API? Did you maybe forget to commit something?

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T21:52:45Z

    Yes you are right, I forget the definition

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-03T22:15:45Z

    OK, I realize now that I misunderstood the concept. I was thinking that a node was able to do self validation. But no, I will have to move my code to the node definition. So let's wait for a new commit...

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-06T06:13:55Z

    @schmittjoh I just push the move to definition and the new abstract class Numeric. Can you review it?

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-10T05:12:59Z

    @schmittjoh, @fabpot
    I fix all the mention points, can you have a look at the final result?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-10T06:38:20Z

    There are still some excessive blank lines if you want to be perfect, but overall looks good now.

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-07-10T07:05:54Z

    @schmittjoh I think the comments of @Baachi are not well placed in the diff. I execute php-cs-fix on all code, so level of perfectness is already good ;)

    @fabpot Do you want some more complements before merging?

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-10T07:07:21Z

    @jeanmonod I'm going to review the code once more and it will be merged for 2.2. Thanks for your work.

    ---------------------------------------------------------------------------

    by fabpot at 2012-09-18T13:58:48Z

    @jeanmonod Can you squash your commits before I merge? Thanks.

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-09-18T14:36:59Z

    @fabpot Squash done

    ---------------------------------------------------------------------------

    by fabpot at 2012-09-19T04:07:13Z

    @jeanmonod One last thing: can you submit a PR on symfony/symfony-docs that update the documentation with the new possibilities?

    ---------------------------------------------------------------------------

    by jeanmonod at 2012-09-20T05:32:01Z

    @fabpot OK, Documentation PR done here: https://github.com/symfony/symfony-docs/pull/1732