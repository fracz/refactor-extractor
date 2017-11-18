commit 908d16904fc9906c781711e79e8f3017a915427a
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Apr 13 10:30:27 2017 +0200

    Refactor CodecConfigurer

    This commit refactors the CodecConfigurer, with it's subtypes
    ServerCodecConfigurer and ClientCodecConfigurerTests, into interfaces
    instead of classes.