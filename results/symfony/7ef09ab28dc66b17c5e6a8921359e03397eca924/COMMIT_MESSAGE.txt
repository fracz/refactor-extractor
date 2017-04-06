commit 7ef09ab28dc66b17c5e6a8921359e03397eca924
Merge: 7474ad5 b269e27
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Feb 22 16:32:31 2012 +0100

    merged branch vicb/config/proto/default (PR #3403)

    Commits
    -------

    b269e27 [Config] Improve handling of PrototypedArrayNode defaults
    4feba09 [Config] implements feedback
    bc122bd [Config] Fix nested prototyped array nodes
    675e5eb [Config] Take advantage of the new PrototypedArrayNode API in the core bundles
    cba2c33 [Config] Improve error messages & extensibility
    bca2b0e [Config] Improve PrototypedArrayNode default value management

    Discussion
    ----------

    [Config] Improve prototype nodes usability, error messages, extensibility

    ### First commit

    *Before* (you should set multiple defalutValues)

    ```php
    <?php
    $root
        ->arrayNode('node')
        ->prototype('array')
            // when the node is not set
            ->defaultValue(array('foo' => 'bar')
            ->children()
                // when the key is not set
                ->scalarNode('foo')->defaultValue('bar')->end()

    $root
        ->arrayNode('node')
        ->prototype('array')
            // when the node is not set
            ->defaultValue(array('defaults' => array('foo1' => 'bar1', 'foo2' => 'bar2')
            ->children()
                ->arrayNode('bar')
                    // when the node is not set
                    ->addDefautsIfNotSet()
                    // when some values are not set (node being set)
                    ->scalarNode('foo1')->defaultValue('bar1')->end()
                    ->scalarNode('foo2')->defaultValue('bar2')->end()
    ```

    *after*

    ```php
    <?php
    $root
        ->arrayNode('node')
        ->addDefaultChildrenWhenNoneSet()
        ->prototype('array')
            ->children()
                ->scalarNode('foo')->defaultValue('bar')->end()

    $root
        ->arrayNode('node')
        ->addDefaultChildrenWhenNoneSet()
        ->prototype('array')
            ->children()
                ->arrayNode('bar')
                    ->scalarNode('foo1')->defaultValue('bar1')->end()
                    ->scalarNode('foo2')->defaultValue('bar2')->end()
    ```

    *more* (exclusive configs)

    ```php
    <?php
    $root
        ->arrayNode('node')
        // Add a default node named 'defaults'
        ->addDefaultChildrenWhenNoneSet()
        // Add a default node named 'foo'
        ->addDefaultChildrenWhenNoneSet('foo')
        // Add two default nodes named 'foo', 'bar'
        ->addDefaultChildrenWhenNoneSet(array('foo', 'bar'))
        // Add two default nodes
        ->addDefaultChildrenWhenNoneSet(2)
    ```

    ### Second commit

    Improves error messages (print the path to the error) & extensibility.

    @schmittjoh I would appreciate you feedback on both the commits. Do you think a boolean $throw switch on `getNode` would make sense (i.e. to prevent throwing excs in prod ?).

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T15:43:18Z

    The error improvements seem uncontroversial.

    I'm not so convinced by the other changes though. What if the prototype is a map and not a simple list?

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T16:07:51Z

    I think there's one caveat left in the code as it is now that I will fix (nested prototypes).

    Could you please give me more details on the use case you are referring to ?

    You do not have to use the new feature but It can be really helpful [here](https://github.com/symfony/symfony/pull/3225/files#L4R38) for example.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T17:20:02Z

    What I mean is something like this:

    ```php
    ->arrayNode("foo")
        ->useAttributeAsKey("name")
        ->prototype(/* ...
    ```

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T17:28:01Z

    What would be wrong then ? (that's the use case I link in my previous msg)

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T17:28:55Z

    How would adding defaults look like?

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T17:36:35Z

    Check the "more" part of the PR message.

    In the linked use case, it would add a "defaults" server using the default host / port / weight. In this case I do not care about the name but the values are important to help alias the equivalent configs. You can override the "defaults" name by using a parameter.

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T17:47:27Z

    ```php
    <?php
    // [...]
        ->arrayNode('servers')
            ->addDefaultChildrenWhenNodeSet()
            ->useAttributeAsKey('name')
            ->prototype('array')
                ->children()
    ```

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T17:47:54Z

    What I was thinking about is having two nodes with different default values. Right now, both nodes while having different keys would still have the same default values which does not make much sense to me. However, we can address this in another PR.

    One thing that we should fix though is that we should require keys in case of a map, and forbid them in case of a list. It might make sense to split it into different methods. Like the following examples make no sense (but are possible atm):

    ```php
    ->arrayNode("foo")
        ->useAttributeAsKey("name")
        ->addDefaultChildrenIfNotSet(5)

    ->arrayNode("foo")
        ->addDefaultChildrenIfNotSet("foo")
        ->prototype("scalar")->end()
    ```

    Another minor nitpick, please rename "when" to "if".

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T18:03:19Z

    @schmittjoh thank you for your feedback.

    message-2:

    * I think the first case is fine (children "1" to "5"). Sometimes you just don't care about the names so it should not be forbidden.
    * I also think the second case is fine as you would write `foo: value` in your config file anyway.

    Let me know your thoughts about the previous statements.

    Agree to change when to if.

    message-1:

    Will change

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T18:06:33Z

    I think "IfNoneSet" is more accurate than "IfNotSet" ?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T18:09:59Z

    If you call "useAttributeAsKey" it automatically means that the keys are meaningful to you (otherwise there is no point in calling it). In such a case, keys should be explicitly given.

    On the other hand, if you do not call it, then the keys are ignored/dropped by the Config component. So if you give a key, it is an obvious error that we should catch. The second case I linked would look like ``foo: [value]`` in contrast to ``foo: { foo: value }``.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-20T18:14:44Z

    I'm not feeling strongly about this, but "IfNotSet" is more consistent with
    "addDefaultsIfNotSet" and basically reads as "if array node is not set, do
    ...". Your example would refer to the children and read as "if none
    (children) have been defined, do ...".

    On Mon, Feb 20, 2012 at 12:06 PM, Victor Berchet <
    reply@reply.github.com
    > wrote:

    > I think "IfNoneSet" is more accurate than "IfNotSet" ?
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/3403#issuecomment-4058579
    >

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T18:30:21Z

    message-2:

    * Agree on first point, will change
    * You could specify the keys in your config file if the prototype is an array (you used a scalar). Should we implement a switch in the validation (i.e. array / not array) or just go with numeric / null arg  as you suggest ?

    message-1:

    > Your example would refer to the children and read as "if none (children) have been defined, do ..."

    QED

    ---------------------------------------------------------------------------

    by vicb at 2012-02-20T22:11:05Z

    @schmittjoh I have implemented your suggestions (other than the "NoneSet"). Let me know if you think this is ok. Thanks.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-02-21T03:24:19Z

    Looks good to me.

    As an additional improvement we might consider to allow to prepopulate an prototyped with values. For example, in the FOSRestBundle there is a case where this could be used.

    ```php
    ->arrayNode('formats')
        ->prepopulateValues(array('application/json' => 'json', 'application/xhtml+xml' => 'xml'))
        ->useAttributeAsKey('name')
        ->prototype('scalar')->canBeUnset()->end()
    ```

    This could be done in a separate PR however and is not strictly related to these improvements.

    ---------------------------------------------------------------------------

    by vicb at 2012-02-21T07:51:59Z

    @schmittjoh that would be a great addition but I think need some thinking (i.e. the name, `initialValues` ?, should we handle duplicates, how - in case we are not using attribue as key, ...) so let's make an other PR, I'd like this one to be merged asap as I need this for the Cache Bundle.

    @fabpot ready