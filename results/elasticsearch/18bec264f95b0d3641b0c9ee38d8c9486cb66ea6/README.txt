commit 18bec264f95b0d3641b0c9ee38d8c9486cb66ea6
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Sep 3 16:01:19 2015 +0200

    Split HasChildQueryParser into toQuery and formXContent

    This is an intial commit that splits HasChildQueryParser / Builder into
    the two seperate steps. This one is particularly nasty since it transports
    a pretty wild InnerHits object that needs heavy refactoring. Yet, this commit
    has still some nocommits and needs more tests and maybe another cleanup but
    it's a start to get the code out there.