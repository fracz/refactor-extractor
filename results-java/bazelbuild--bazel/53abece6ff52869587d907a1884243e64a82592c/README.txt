commit 53abece6ff52869587d907a1884243e64a82592c
Author: Ulf Adams <ulfjack@google.com>
Date:   Wed Feb 3 08:30:07 2016 +0000

    Correctly flag loading errors in the interleaved case.

    In the interleaved case, loading errors can now also be discovered during the
    analysis phase. Add a boolean flag to the SkyframeAnalysisResult to indicate
    that such an error happened, and pass it through in BuildView.

    Also refactor BuildView to simplify the code a bit - simply pass the
    SkyframeAnalysisResult to the createResult method.

    There is already an integration test for this - I'm adding a faster unit test
    in BuildViewTest, as this is part of the BuildView contract.

    --
    MOS_MIGRATED_REVID=113716870