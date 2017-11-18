commit 5674a4a1e9d7823bdd879b2652355cb3543e32d3
Author: blickly <blickly@google.com>
Date:   Thu Jul 20 15:56:05 2017 -0700

    Small refactorings to ConvertToTypedInterface

    * Refactor FileInfo usage to ensure one unique instance per file.
    * Remove unnecessary references to the compiler object.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=162678190