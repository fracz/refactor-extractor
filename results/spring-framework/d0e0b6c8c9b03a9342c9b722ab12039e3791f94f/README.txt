commit d0e0b6c8c9b03a9342c9b722ab12039e3791f94f
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Mar 21 14:48:36 2017 -0400

    Minor Jackson encoder/decoder refactoring

    Consolidate JsonView hint extraction in shared base class.

    Rename base class from AbstractJackson2Codec to Jackson2CodecSupport
    since the class mainly provides support methods.