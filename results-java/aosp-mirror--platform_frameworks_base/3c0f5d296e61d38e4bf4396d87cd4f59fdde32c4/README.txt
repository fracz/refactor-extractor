commit 3c0f5d296e61d38e4bf4396d87cd4f59fdde32c4
Author: Andy Hung <hunga@google.com>
Date:   Mon May 15 15:41:14 2017 -0700

    VolumeShaper: Enable xOffset from Java

    Previously only accessible from native.
    Also improve documentation and comments.

    Test: CTS VolumeShaperTest, Ducking
    Bug: 38353147
    Change-Id: I27bb34f0a5d28f80d138111bbeeb92653b5195c5