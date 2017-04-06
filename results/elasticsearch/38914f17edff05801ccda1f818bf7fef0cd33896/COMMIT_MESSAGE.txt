commit 38914f17edff05801ccda1f818bf7fef0cd33896
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Dec 19 19:32:10 2016 +0100

    [TEST] improve ElasticsearchAssertions#assertEquivalent for ToXContent

    Rename the method to assertToXContentEquivalent to highlight that it's tailored to ToXContent comparisons.

    Rather than parsing into a map and replacing byte[] in both those maps, add custom equality assertions that recursively walk maps and lists and call Arrays.equals whenever a byte[] is encountered.