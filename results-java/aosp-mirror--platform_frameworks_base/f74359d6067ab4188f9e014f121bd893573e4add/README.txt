commit f74359d6067ab4188f9e014f121bd893573e4add
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Wed Nov 11 10:12:04 2015 -0800

    Fix thumbnail animation appearing on going home transition.

    We were not dilligent enough about clearing old state in AppTransition,
    so parts of old overrides would bleed to the new state. In order to
    improve the situation, we have now a single clear method, that will
    clear all state that should be restricted to a single transition.

    Bug: 25630796
    Change-Id: Icb402d05aaa2d1bd356d55e43502442d8fd8cd23