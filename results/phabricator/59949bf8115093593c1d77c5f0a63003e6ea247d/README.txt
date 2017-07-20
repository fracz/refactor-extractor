commit 59949bf8115093593c1d77c5f0a63003e6ea247d
Author: epriestley <git@epriestley.com>
Date:   Wed Jul 25 11:51:27 2012 -0700

    Add "device" JS/CSS for reactive stuff

    Summary:
    As we work through @chad's redesign, one thing I want to do is improve the tablet/mobile experience.

    Add a "device" behavior which sets a "device-phone", "device-tablet" or "device-desktop" class on the root div. The behavior (device names, width triggers) is mostly based on Bootstrap.

    Also adds a preview viewport=meta tag, which makes the iPhone not scale the page like crazy and is a desirable end state, but currently makes the app less usable since things get cut off.

    Test Plan:
    Added some classes like this:

            .device-desktop {
              background: blue;
            }
            .device-tablet {
              background: orange;
            }
            .device-phone {
              background: yellow;
            }

    ...and loaded the site on a desktop, iPad and iPhone. Resized the window. Got the right background color in all cases.

    Reviewers: btrahan, chad

    Reviewed By: chad

    CC: aran

    Differential Revision: https://secure.phabricator.com/D3063