commit 36d12dde5b050363d9bf0548b98c44cd622669d3
Merge: 4dd47ac 12bdec3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 14 18:10:40 2012 +0200

    merged branch stof/serializer_improvement (PR #4904)

    Commits
    -------

    12bdec3 Moved the NormalizationAwareInterface check to the ChainEncoder
    28e137c [Serializer] Added a ChainEncoder and a ChainDecoder

    Discussion
    ----------

    [Serializer] Added a ChainEncoder and a ChainDecoder

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/stof/symfony.png?branch=serializer_improvement)](http://travis-ci.org/stof/symfony)
    Fixes the following tickets: -
    Todo: -

    These classes contains the logic previously defined in the Serializer itself to handle the choice of a serializer. This allows reusing it when using only the encoding part of the component, without having to use the Serializer class (which is not as handy to bootstrap when you want to use only encoders and decoders as normalizers come first)

    I was wondering if these classes should have adders but I kept the constructor injection only to be consistent with the current code (encoders cannot be registered after the instantiation) and to avoid implementing the SerializerAwareInterface in them (to allow injecting the Serializer in serializer-aware encoders and decoders added later).

    Note that this change is fully BC as it only changes the internal implementation of the Serializer.

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-14T11:07:32Z

    ping @lsmith77 @Seldaek

    ---------------------------------------------------------------------------

    by Seldaek at 2012-07-14T15:17:42Z

    After a quick look, I'd say +1