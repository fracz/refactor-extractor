commit 20d94749043d0851f1da10c7749fd7eb13a35081
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu May 29 18:35:45 2014 -0700

    More work on voice interaction visuals.

    There is now a special theme for voice interaction activities
    to use, so they can be a panel that is better intergrated with
    the rest of the voice interaction experience.  This is still
    not completely working, I have some hacks in the demo app to
    get it right; I'll fix that in a future change.

    Also improve VoiceInteractor to be retained across activity
    instances, for things like rotation.

    And bump up the number of concurrent broadcasts that are allowed
    on non-svelte devices, since they can handle more and this makes
    the boot experience better when dispatching BOOT_COMPLETED.

    Change-Id: Ie86b5fd09b928da20d645ec2200577dee3e6889d