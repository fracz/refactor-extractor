commit 847a4dec130a972e2417e060f6dfde33c054d8d1
Author: Adam Murdoch <adam@gradle.com>
Date:   Wed Aug 2 17:17:06 2017 +1000

    Added a tiny bit of validation that the `Provider` used to define a `PublishArtifact` holds a value that can be converted to a `File`, rather than failing with a `ClassCastException`. This should be a lot stronger, but can be improved later.