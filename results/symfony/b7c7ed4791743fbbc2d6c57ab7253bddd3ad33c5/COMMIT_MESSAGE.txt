commit b7c7ed4791743fbbc2d6c57ab7253bddd3ad33c5
Merge: a6cdddd 0776b50
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Dec 14 19:34:07 2011 +0100

    merged branch lsmith77/serializer_interface (PR #2530)

    Commits
    -------

    0776b50 removed supports(De)Serializiation()
    72b9083 SerializerAwareNormalizer now only implements SerializerAwareInterface
    97389fa use Serializer specific RuntimeException
    cb495fd added additional unit tests for deserialization
    967531f fixed various typos from the refactoring
    067242d updated serializer tests to use the new interfaces
    d811e29 CS fix
    351eaa8 require a (de)normalizer inside the (de)normalizable interfaces instead of a serializer
    c3d6123 re-added supports(de)normalization()
    078f7f3 more typo fixes
    c3a711d abstract class children should also implement dernormalization
    2a6741c typo fix
    d021dc8 refactored encoder handling to use the supports*() methods to determine which encoder handles what format
    f8e2787 refactored Normalizer interfaces
    58bd0f5 refactored the EncoderInterface
    b0daf35 split off an EncoderInterface and NormalizerInterface from SerializerInterface

    Discussion
    ----------

    [Serializer] split off an EncoderInterface and NormalizerInterface from SerializerInte

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: yes (but not inside a stable API)
    Symfony2 tests pass: ![Build Status](https://secure.travis-ci.org/lsmith77/symfony.png?branch=serializer_interface)
    Fixes the following tickets: #2153

    The purpose is to make it easier for other implementations that only implement parts of the interface due to different underlying approaches like the JMSSerializerBundle.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/11/01 03:36:17 -0700

    Actually, you can keep the current interface and I will just provide an adapter, sth like the following:

    ```php
    <?php

    class SymfonyAdapter implements SymfonyInterface
    {
        public function __construct(BundleInterface $serializer) { /* ... */ }
        // symfony serializer methods mapped to bundle methods
    }
    ```
    I like to provide an adapter instead of implementing the interface directly since the bundle can be used standalone right now, and I don't want to add a dependency on the component just for the sake of the interface.

    However, I do not completely see the purpose of the component. When would someone be recommended to use it? Everything the component does, the bundles does at the same level with the same complexity or simplicity (however you want to view that).

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/01 03:40:55 -0700

    standalone in what way? you mean even out of the context of Symfony? In that context imho you should ship that code outside of a Bundle.

    Regardless, how will that adaptor work? How would you implement methods like ``getEncoder()``? Afaik you can't and this is what this PR is about, splitting the interface to enable people to more finely specify what they provide.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/11/01 04:03:56 -0700

    I would just throw exceptions when something is not supported.

    The more important question though is what is the goal of the component in the long-term, i.e. what problems is it supposed to solve, or in which cases should it be used?

    Because right now it seems to me - correct me if I'm wrong - that the only purpose is that people don't have to install an extra library. However, that might even be frustrating for users because they need to migrate their code to the bundle as soon as they need to customize the serialization process which you need in 99% of the cases. For deserialization, the situation in the component is even worse. So, if my assessment is correct here (i.e. component to get started fast, if you need more migrate to the bundle), I think it would be better and less painful to have them start with the bundle right away.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/01 04:15:10 -0700

    Well then imho it would be better to split the interface.

    I think the serializer component is sufficient for many situations and imho its easier to grok. Furthermore the normalizer/encoder concept it can be used in situations where JMSSerializerBundle cannot be used.

    And splitting up the interfaces has exactly the goal of reducing the "frustrations" caused by out growing the the component.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/11/01 04:29:39 -0700

    I don't agree, but it's a subjective thing anyway.

    So, whatever interface you come up with (preferably as few methods as possible), I will provide an adapter for it.

    ---------------------------------------------------------------------------

    by fabpot at 2011/11/07 08:45:25 -0800

    What's the status of this PR?

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/07 10:28:14 -0800

    from my POV its good to go. but would like a nod from someone else in terms of the naming of the new interfaces

    On 07.11.2011, at 17:45, Fabien Potencier <reply@reply.github.com> wrote:

    > What's the status of this PR?
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/2530#issuecomment-2655889

    ---------------------------------------------------------------------------

    by stof at 2011/11/08 11:37:40 -0800

    @lsmith77 what about doing the same for the ``NormalizerInterface`` instead of adding a new interface with a confusing name ? The Serializer class could implement ``Normalizer\NormalizerInterface`` by adding the 2 needed methods instead of duplicating part of the interface.

    The next step is to refactor the Serializer class so that it choose the encoder and the decoder based on the ``support*`` methods. But this could probably be done in a separate PR.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/08 11:51:27 -0800

    yeah .. i wanted to do that once we are in agreement on the encoder stuff. question then is if we should again split off Denormalization. i guess yes.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/08 12:06:34 -0800

    ok done ..

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/08 12:59:51 -0800

    i guess the next big task is to add more tests .. had to fix way too few unit tests with all this shuffling around .. will also help validating the concept. i should also test this out in a production application.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/11/14 13:27:48 -0800

    @ericclemmons can you also have a look at this PR and potentially help me adding tests?

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/07 07:32:06 -0800

    @lsmith77: Is it ready to be merged? Should I wait for more unit tests?

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/07 07:34:56 -0800

    If you merge it I am afraid I might get lazy and not write tests. This is why I changed the topic to WIP. I promise to finish this on the weekend.

    Note however I was planning to write the tests for 2.0 and send them via a separate PR.
    Once that PR is merged into 2.0 and master. I would then refactor them to work for this PR.
    This way both 2.0 and master will have tests.

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/07 07:42:15 -0800

    @lsmith77: sounds good. Thanks.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/11 12:02:12 -0800

    @fabpot ok i am done from my end.
    @schmittjoh would be great if you could look over the final interfaces one time and give your blessing that you will indeed be able to provide implementations for these interfaces inside JMSSerializerBundle (even if just via an adapter)

    ---------------------------------------------------------------------------

    by stof at 2011/12/12 12:43:49 -0800

    @schmittjoh can you take a look as requested by @lsmith77 ?

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/12/13 03:33:23 -0800

    Are the supports methods necessary? This is what I'm using in the bundle:
    https://github.com/schmittjoh/JMSSerializerBundle/blob/master/Serializer/SerializerInterface.php

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/13 04:08:49 -0800

    @schmittjoh without them determining if something is supported will always require an exception, which is pretty expensive. especially if one iterates over a data structure this can cause a lot of overhead.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/12/13 04:24:18 -0800

    my question was more if you have a real-world use case where this is useful?

    On Tue, Dec 13, 2011 at 1:08 PM, Lukas Kahwe Smith <
    reply@reply.github.com
    > wrote:

    > @schmittjoh without them determining if something is supported will always
    > require an exception, which is pretty expensive. especially if one iterates
    > over a data structure this can cause a lot of overhead.
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/2530#issuecomment-3122157
    >

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/13 04:28:08 -0800

    yes .. this serializer .. since it traverses the tree and decides what is the current normalizer one by one (aka not via visitors as in your implementation). without the supports*() methods it would need to have the normalizer throw exceptions, but this is not exceptional, its the normal code flow to have to iterate to find the correct normalizer.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/12/13 04:30:36 -0800

    can we split it off into a second interface?

    On Tue, Dec 13, 2011 at 1:28 PM, Lukas Kahwe Smith <
    reply@reply.github.com
    > wrote:

    > yes .. this serializer .. since it traverses the tree and decides what is
    > the current normalizer one by one (aka not via visitors as in your
    > implementation). without the supports*() methods it would need to have the
    > normalizer throw exceptions, but this is not exceptional, its the normal
    > code flow to have to iterate to find the correct normalizer.
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/2530#issuecomment-3122315
    >

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/13 04:33:27 -0800

    hmm .. i guess we could .. these methods in a way are implementation specific and are mainly public because its different objects interacting with each other, though for users of the lib they can also be convenient at times.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/14 09:13:53 -0800

    ok i reviewed things again and just removed those two methods, since the possibility for these methods to be feasible is too tied to the implementation and for this particular implementation supportsEncoding() and supportsDecoding() are still available.

    so all ready to be merged ..

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/14 09:15:44 -0800

    hmm i realized one thing just now:
    https://github.com/lsmith77/symfony/commit/cb495fd7a36fe7f895bec1686e8742ae5b5cb550

    that commit should also be included in 2.0 .. i am not sure what the most elegant way is to make that happen ..

    ---------------------------------------------------------------------------

    by fabpot at 2011/12/14 10:10:16 -0800

    @lsmith77: commit cb495fd7a36fe7f895bec1686e8742ae5b5cb550 cannot be cherry picked in 2.0 as is as the tests do not pass:  "Fatal error: Call to undefined method Symfony\Component\Serializer\Serializer::supportsDenormalization() in tests/Symfony/Tests/Component/Serializer/SerializerTest.php on line 150"

    ---------------------------------------------------------------------------

    by lsmith77 at 2011/12/14 10:11:55 -0800

    ah of course .. i just removed that method :) .. then never mind .. all is well.