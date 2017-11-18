commit dc279d839bab457561caf2b5c677e53f10d06c99
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Apr 13 11:01:46 2017 +0200

    Use refactored CodecConfigurer

    This commit changes the `HandlerStrategies` and `ExchangeStrategies`
    builders to use the `CodecConfigurer` for configuring Decoder|Encoder
    and HttpMessage[Reader|Writer]. Other classes that use `CodecConfigurer`
    have also been changed to reflect the refactoring to interfaces.

    This commit also removes the ExchangeStrategies methods that take an
    application context, as it was too naive approach to simply look up
    every message reader and writer in the context.

    Issue: SPR-15415, SPR-15435