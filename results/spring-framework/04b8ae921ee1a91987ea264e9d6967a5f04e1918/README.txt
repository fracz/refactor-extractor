commit 04b8ae921ee1a91987ea264e9d6967a5f04e1918
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Oct 3 17:33:31 2016 +0200

    Introduce getContentAsByteArray()/getContentAsString() in MockHtttpSvltReq

    In order to improve debugging and logging within test suites, this
    commit introduces getContentAsByteArray() and getContentAsString()
    methods in MockHttpServletRequest, analogous to the existing methods in
    MockHttpServletResponse.

    Issue: SPR-14717