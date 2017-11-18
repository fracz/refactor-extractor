commit 7170ff380c4570c9a3fc6f65e522b2ae182f28e6
Author: olly <olly@google.com>
Date:   Mon Apr 11 04:18:20 2016 -0700

    Remove V1 DASH multi-period + seeking-in-window from V2.

    Both of these features are being promoted to first class
    citizens in V2 (multi-period support will be handled via
    playlists, seeking-in-window will be handled by exposing
    the window/timeline from the player and via the normal
    seek API). For now, it's much easier to continue the
    refactoring process with the features removed.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=119518675