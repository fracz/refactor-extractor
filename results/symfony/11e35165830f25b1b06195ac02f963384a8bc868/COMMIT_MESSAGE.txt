commit 11e35165830f25b1b06195ac02f963384a8bc868
Merge: 7eb22a7 411a0cc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Feb 9 13:39:47 2012 +0100

    merged branch blogsh/dynamic_group_sequence (PR #3199)

    Commits
    -------

    411a0cc [Validator] Added GroupSequenceProvider to changelog
    815c769 [Validator] Renamed getValidationGroups to getGroupSequence
    d84a2e4 [Validator] Updated test expectations
    9f2310b [Validator] Fixed typos, renamed hasGroupSequenceProvider
    e0d2828 [Validator] GroupSequenceProvider tests improved, configuration changed
    c3b04a3 [Validator] Changed GroupSequenceProvider implementation
    6c4455f [Validator] Added GroupSequenceProvider

    Discussion
    ----------

    [Validator] Added GroupSequenceProvider

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: ![](https://secure.travis-ci.org/blogsh/symfony.png?branch=dynamic_group_sequence)

    As discussed in #3114 I implemented the "GroupSequenceProvider" pattern for the validator component. It allows the user to select certain validation groups based on the current state of an object. Here is an example:

        /**
         * @Assert\GroupSequenceProvider("UserGroupSequnceProvider")
         */
        class User
        {
            /**
             * @Assert\NotBlank(groups={"Premium"})
             */
            public function getAddress();

            public function hasPremiumSubscription();
        }

        class UserGroupSequenceProvider implements GroupSequenceProviderInterface
        {
            public function getValidationGroups($user)
            {
                if ($user->hasPremiumSubscription()) {
                    return array('User', 'Premium');
                } else {
                    return array('User');
                }
            }
        }

    With this patch there are two mechanisms to define the group sequence now. Either you can use @GroupSequence to define a static order of validation groups or you can use @GroupSequenceProvider to create dynamic validation group arrays.
    The ClassMetadata therefore has methods now which implement quite similar things. The question is whether it would make sense to interpret the static group sequence as a special case and create something like a DefaultGroupSequenceProvider or StaticGroupSequenceProvider which is assigned by default. This would cause a BC break inside the validator component.

    ---------------------------------------------------------------------------

    by bschussek at 2012-01-28T13:39:54Z

    I like the implementation, but I think we should differ a little bit from Java here.

    1. `GroupSequenceProviderInterface` should be implemented by the domain classes themselves (`User`), not by a separate class.
    2. As such, the parameter `$object` from `getValidationGroups($object)` can be removed
    3. `ClassMetadata::setGroupSequenceProvider()` should accept a boolean to activate/deactivate this functionality. Also the check for the interface (does the underlying class implement it?) should be done here

    Apart from that, special cases need to be treated:

    * A definition of a group sequence and a group sequence provider in the same `ClassMetadata` should not be allowed. Either of them must not be set.
    * Metadata loaders must take care of settings made by parent classes. If `Animal` is extended by `Dog`, `Animal` defines a group sequence (or group sequence provider) and `Dog` a group sequence provider (or group sequence), only the setting of `Dog` should apply

    ---------------------------------------------------------------------------

    by blogsh at 2012-01-28T21:25:37Z

    Changes of the latest commit:

    - GroupSequenceProviderInterface has to be implemented by the domain class
    - The annotation/configuration options let the user define whether the provider is activated or not (is this neccessary at all?)
    - An error is thrown if the user wants to use static group sequences and the provider simultaneously

    At the moment neither the static group sequence nor the provider is inherited from parent classes or interfaces. I don't know if it would make sense to enable this feature. There could be problems if a user wants to define a static group sequence in the parent class and a sequence provider in the child class.

    ---------------------------------------------------------------------------

    by bschussek at 2012-01-30T13:07:04Z

    > There could be problems if a user wants to define a static group sequence in the parent class and a sequence provider in the child class.

    In this case, the setting in the child class should override the setting of the parent class.

    But we can leave this open for now. As it seems, [this issue is unresolved in Hibernate as well](https://hibernate.onjira.com/browse/HV-467).

    ---------------------------------------------------------------------------

    by blogsh at 2012-01-30T22:54:41Z

    Okay, finally I managed to upload the latest commit. If you got a bunch of notifications or so I'm sorry, but I had to revert some accidental changes in the commit :(

    I've rewritten the tests and have removed the "active" setting in the XML configuration.

    ---------------------------------------------------------------------------

    by blogsh at 2012-02-02T15:24:01Z

    Okay, typos are fixed now and `hasGroupSequenceProvider` has been renamed to `isGroupSequenceProvider`. I also had to adjust some tests after the rebase with master.

    ---------------------------------------------------------------------------

    by bschussek at 2012-02-03T09:25:19Z

    Looks good.

    @fabpot :+1:

    ---------------------------------------------------------------------------

    by fabpot at 2012-02-03T09:46:52Z

    Can you add a note in the CHANGELOG before I merge? Thanks.

    ---------------------------------------------------------------------------

    by blogsh at 2012-02-09T12:31:27Z

    @fabpot done