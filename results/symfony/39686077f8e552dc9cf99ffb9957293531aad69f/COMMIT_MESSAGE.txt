commit 39686077f8e552dc9cf99ffb9957293531aad69f
Merge: 357d747 6b1652e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jan 10 19:35:24 2013 +0100

    merged branch bschussek/property-path (PR #6595)

    This PR was merged into the master branch.

    Commits
    -------

    6b1652e [PropertyAccess] Property path, small refactoring, read/writeProperty to read/write Property/Index.
    1bae7b2 [PropertyAccess] Extracted PropertyAccess component out of Form

    Discussion
    ----------

    [PropertyAccess] Extracted PropertyAccess component out of Form

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: -

    TODO: adapt DoctrineBundle/PropelBundle to pass the "property_accessor" service to EntityType/ModelType

    Usage:

    ```php
    $accessor = PropertyAccess::getPropertyAccessor();

    // equivalent to $object->getFoo()->setBar('value')
    $accessor->setValue($object, 'foo.bar', 'value');

    // equivalent to $object->getFoo()->getBar()
    $accessor->getValue($object, 'foo.bar');

    // equivalent to $object->getFoo()['bar']
    $accessor->getValue($object, 'foo[bar]');

    // equivalent to $array['foo']->setBar('value')
    $accessor->setValue($array, '[foo].bar', 'value');

    // equivalent to $array['foo']['bar']
    $accessor->getValue($array, '[foo][bar]');
    ```

    Later on, once we have generation and caching of class-specific accessors, configuration will be something like this (consistent with the Form and Validator component):

    ```php
    $accessor = PropertyAccess::getPropertyAccessorBuilder()
        ->setCacheDirectory(__DIR__ . '/cache')
        ->setCacheLifeTime(86400)
        ->enableMagicGetSet()
        ->enableMagicCall()
        ->getPropertyAccessor();
    ```

    or

    ```php
    $accessor = PropertyAccess::getPropertyAccessorBuilder()
        ->setCache($cache)
        ->getPropertyAccessor();
    ```

    etc.

    ---------------------------------------------------------------------------

    by Burgov at 2013-01-07T08:48:15Z

    +1. I use this feature outside of the Form context a lot

    ---------------------------------------------------------------------------

    by stof at 2013-01-07T08:49:34Z

    The classes in the Form component should be kept for BC (and deprecated) for people using the feature

    ---------------------------------------------------------------------------

    by michelsalib at 2013-01-07T10:02:19Z

    YES YES YES :+1:. Sorry for my enthusiasm, but I already copy pasted the PropertyPath class to some of my libraries to avoid linking to the whole Form component. I thus will be glad to officially use this component into my libraries via composer.

    ---------------------------------------------------------------------------

    by norzechowicz at 2013-01-07T10:17:39Z

    Same as @michelsalib to avoid linking full Form component I was using copied parts of code. Can't wait to use this component in my lib. :+1:

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T10:43:41Z

    I split away `getValue()` and `setValue()` from `PropertyPath` into a new class `ReflectionGraph`. The component is also named ReflectionGraph now.

    ---------------------------------------------------------------------------

    by michelsalib at 2013-01-07T10:47:10Z

    I am not found of the name. What do you intend to do in the component more than what PropertyPath does ?

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T10:58:59Z

    @michelsalib A `PropertyPath` is simply a string like `foo.bar[baz]`. `getValue()` and `setValue()` interpret this path. There may be different interpretations for the same path, so these methods were split into a new class.

    I chose the name `ReflectionGraph` because the functionality is very similar to `ReflectionProperty`.

    ```php
    $reflProperty = new ReflectionProperty('Vendor/Class', 'property');
    $reflProperty->setValue($object, 'foo');

    $reflGraph = new ReflectionGraph();
    $reflGraph->setValue($object, 'property.path', 'foo');
    ```

    ---------------------------------------------------------------------------

    by michelsalib at 2013-01-07T11:00:42Z

    What about naming it `Reflection`, maybe sometime we will want to add more reflection tools for classes, interfaces... ?

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T11:02:32Z

    @michelsalib I doubt that we will do that. PHP's implementation is sufficient.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T11:03:57Z

    > Backwards compatibility break: no

    Really ?

    ---------------------------------------------------------------------------

    by michelsalib at 2013-01-07T11:05:07Z

    Well, that is just a suggestion. If I am the only one to oppose, I won't complain.

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T11:09:08Z

    > Really ?

    @vicb Would you please refrain from such meanginless comments in the future? I'm getting a bit tired of them. If you think that BC is broken somewhere, tell me where so that I can fix it.

    ---------------------------------------------------------------------------

    by stof at 2013-01-07T11:09:43Z

    @vicb There is no BC break as he kept deprecated classes for BC

    ---------------------------------------------------------------------------

    by norzechowicz at 2013-01-07T11:13:12Z

    @bschussek what do you think about some kind of factory for Reflection? This will prevent creating new Reflection objects each time you want to access properties values.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T11:18:47Z

    @bschussek my point is that my comment is no more meaningless than closing #6453 because it will break BC.We could also keep BC by extending the classes in the new ns but in both cases BC will ultimately be broken (when the legacy classes are removed)

    ---------------------------------------------------------------------------

    by vicb at 2013-01-07T12:23:45Z

    @bschussek @stof I think that modifying the constructor signatures of `EntityChoiceList`, `FormType` are BC breaks (this is not an exhaustive list)

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-07T12:35:13Z

    @vicb You are right. I added corresponding entries to the CHANGELOG and adapted the above description.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-08T13:39:13Z

    @bschussek looking at this PR, I was wondering if an alternate syntax would make sense:

    ```php
    <?php
    $reflGraph = new ReflectionGraph($object);

    // equivalent to $object->getFoo()->setBar('value')
    $reflGraph['foo.bar'] = 'value';

    // equivalent to $object->getFoo()->getBar()
    $reflGraph['foo.bar'];
    ```

    _Sorry for the off topic_

    ---------------------------------------------------------------------------

    by vicb at 2013-01-08T13:49:46Z

    The advantage of using such a `ReflectionGraph` factory is that it might be easier to return specialized reflection graphs, ie optimized instances (that would be cached).

    ---------------------------------------------------------------------------

    by Toflar at 2013-01-08T14:49:54Z

    I was also puzzled by the fact that there will be many `ReflectionGraph` instances although they don't have to. I'm with @vicb and I'd also vote for using the constructor to set the subject you're working on. Otherwise you'll repeat yourself over and over again by passing the subject - say `$object` - to `getValue()` or `setValue()`. If however you don't like the constructor thing then why do we have to have an instance of `ReflectionGraph` rather than just go for static methods and use `ReflectionGraph::getValue()` and `ReflectionGraph::setValue()`?

    In my opinion there are a few methods that could be static anyway (especially some private ones) :)

    But probably I misunderstood something as I'm just about to discover the SF components and don't have any experience working with them (so basically I just read the PR because of @bschussek's tweet :D)

    Couldn't come up with any intuitive name for the component though :(
    Generally when we talk about "getting" and "setting" values we call those things "mutators"...so `GraphMutator` might be more intuitive than the word `Reflection` :)

    ---------------------------------------------------------------------------

    by Taluu at 2013-01-08T14:57:42Z

    I like the last proposition made by @vicb (implementing `ArrayAccess` on `ReflectionGraph` - or whatever name will be chosen (`PathMutator` for example :D), and also specify which object should be worked on in the constructor rather than in each method).

    Would this also be used in the `Validator` component ?

    ---------------------------------------------------------------------------

    by stof at 2013-01-08T15:16:12Z

    @Toflar A static ``ReflectionGraph::getValue()`` means you have a coupling to the implementation (as with any static call). The current implementation allows you to replace it with your own implementation as long as you implement the interface as it follows the DI pattern (as done in other places in Symfony).

    @vicb The issue with ``$reflGraph = new ReflectionGraph($object);`` is that you cannot inject the ReflectionGraph anymore, as you need a new one each time. This would mean adding a ``ReflectionGraphFactory`` to be injected (and which could then be replaced by a factory using code generation). Using the constructor directly would not allow using a replacement based on code generation later. So the resulting code would more likely be

    ```php

    $reflGraph = new ReflectionGraph();

    $mutator = $reflGraph->getMutator($object);

    // equivalent to $object->getFoo()->setBar('value')
    $mutator['foo.bar'] = 'value';

    // equivalent to $object->getFoo()->getBar()
    $mutator['foo.bar'];
    ```

    Btw, writing this, I find the naming Mutator suggested by @Taluu good when it concerns the setter, but quite weird when getting the value.

    ---------------------------------------------------------------------------

    by Taluu at 2013-01-08T15:21:00Z

    I was not the one to suggest though, it was @everzet. But then something like `PathAccessor`, as it is both a mutator and a getter ? I also like @stof suggestion, still in the idea of avoiding to have to put the object as an argument and also allowing to use an `ArrayAccess` interface..

    ---------------------------------------------------------------------------

    by vicb at 2013-01-08T15:21:54Z

    @stof your remark makes sense.

    What about `Accessor`, the benefit being that it might well be the name of a coming PHP feature: https://wiki.php.net/rfc/propertygetsetsyntax-v1.2

    ---------------------------------------------------------------------------

    by everzet at 2013-01-08T15:27:02Z

    ```php
    $manager = new PropertyManager(new PropertyPath());
    $num = $manager->getValue($object, 'foo.num');
    $manager->setValue($object, 'foo.num', $num + 1);

    $objectManager = new ObjectPropertyManager($object[, $manager]);
    $num = $objectManager->getValue('foo.num');
    $objectManager->setValue('foo.num', $num + 1);

    $objectManager['foo.num'] += 1;
    ```

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-08T15:28:01Z

    It might be me, but I don't like `ArrayAccess` to be misused for features like that. If I access a key in an array access structure, I expect it to be something like a collection, an associative array or a key value store. This class is neither.

    Putting that aside, an accessor for a specific object might make sense, but I'm not sure about that yet.

    ```php
    $reflObject->setValue('foo.bar', 'value');
    $reflObject->getValue('foo.bar');
    ```

    ---------------------------------------------------------------------------

    by stof at 2013-01-08T15:28:52Z

    @vicb I would vote for PathAccessor then, as we are not doing simple accessor but accessors through a path in an object graph.
    In my snippet above, we would then have a ReflectionGraph instance and a PathAccessor instance (``$mutator``).

    Btw, I would also keep the methods ``setValue`` and ``getValue``. I find it more clear.

    ---------------------------------------------------------------------------

    by bschussek at 2013-01-08T15:32:07Z

    @stof But then we're rather left with the question of ReflectionGraph **vs.** PathAccessor. I don't think that the tiny interface difference (one global, one object-based) justifies the big naming difference.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-08T15:33:24Z

    > This class is neither.

    It might be `$pa['foo.bar[baz]'] = $pa['foo.bar']['baz'];` I don't know if it would help though.

    ---------------------------------------------------------------------------

    by stof at 2013-01-08T15:35:51Z

    @bschussek In my suggestion, ``ReflectionGraph`` is a factory for the PathAccessor objects. It is not accessing anymore itself (which would probably continue to cause issues to implement it with code generation). But the naming could indeed be changed to something else.