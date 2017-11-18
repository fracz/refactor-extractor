commit a6bfe02d24bba6dade58a7a996ddb0959307b8bc
Author: Oliver Woodman <olly@google.com>
Date:   Fri Nov 27 16:38:30 2015 +0000

    Add additional Widevine samples + improve errors.

    * Add additional Widevine samples.
    * Improve error messaging in demo app around decoders.
    * Display toasts for playback errors related to missing insecure
      decoders, missing secure decoders, decoder instantiation failure
      and decoder query failure.
    * Remove checks from SampleChooserActivity, since the above largely
      covers off this problem.