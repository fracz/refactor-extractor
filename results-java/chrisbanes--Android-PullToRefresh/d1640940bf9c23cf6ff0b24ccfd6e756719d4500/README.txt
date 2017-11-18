commit d1640940bf9c23cf6ff0b24ccfd6e756719d4500
Author: Chris Banes <chris@senab.co.uk>
Date:   Thu Oct 18 12:30:29 2012 +0100

    Massively improve how the internal state is set and propagated internally.

    Two methods have been renamed, with the old methods deprecated.
    OnPullEventListener has been improved so that it calls back with all state changes.
    SoundPullEventListener has been improved as above.