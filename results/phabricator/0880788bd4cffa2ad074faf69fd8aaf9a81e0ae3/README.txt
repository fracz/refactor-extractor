commit 0880788bd4cffa2ad074faf69fd8aaf9a81e0ae3
Author: epriestley <git@epriestley.com>
Date:   Tue Apr 7 14:38:03 2015 -0700

    Restructure cache checks to improve modularity

    Summary:
    Ref T5501. This code was headed down a bad road; dump an indirection layer between rendering and data gatehring.

    In particular, this will make it much easier to lift these issues into setup warnings eventually.

    Test Plan: Viewed cache status page.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T5501

    Differential Revision: https://secure.phabricator.com/D12315