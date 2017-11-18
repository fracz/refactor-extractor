commit 1b5a2a96f793211bfbd39aa29cc41031dfa23950
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Sat Jun 18 18:34:16 2011 -0700

    Read "qtaguid" network stats, refactor templates.

    Teach NMS to read qtaguid stats from kernel, but fall back to older
    stats when kernel doesn't support.  Add "tags" to NetworkStats entries
    to support qtaguid.  To work around double-reporting bug, subtract
    tagged stats from TAG_NONE entry.

    Flesh out stronger NetworkTemplate.  All NetworkStatsService requests
    now require a template, and moved matching logic into template.

    Record UID stats keyed on complete NetworkIdentitySet definition,
    similar to how interface stats are stored.  Since previous UID stats
    didn't have iface breakdown, discard during file format upgrade.

    Change-Id: I0447b5e7d205d73d28e71c889c568e536e91b8e4