commit 76fe5f6fce3a62b12537415e49d4c3ed17af9393
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Sat Mar 18 10:35:18 2017 -0400

    ResourceHttpMessageWriter refactoring

    Fold ResourceRegionHttpMessageWriter into ResourceHttpMessageWriter.
    The latter was a private helper (not meant to be exposed) and the two
    have much in common now sharing a number of private helper methods.

    The combined class does not extend AbstractServerHttpMessageConverter
    from which it was not using anything.

    Internally the combined class now delegates directly to ResourceEncoder
    or ResourceRegionEncoder as needed. The former is no longer wrapped
    with EncoderHttpMessageWriter which is not required since "resource"
    MediaType determination is a bit different.

    The consolidation makes it easy to see the entire algorithm in one
    place especially for server side rendering (and HTTP ranges). It
    also allows for consistent determination of the "resource" MediaType
    via MediaTypeFactory for all use cases.