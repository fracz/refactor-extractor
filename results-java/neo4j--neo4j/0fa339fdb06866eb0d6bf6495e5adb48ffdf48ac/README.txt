commit 0fa339fdb06866eb0d6bf6495e5adb48ffdf48ac
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Apr 11 21:07:39 2017 +0200

    Utilizes available memory and imports multiple types at a time

    Instead of only a single type at a time. This re-introduces caching for
    keeping relationship heads for multiple types per dense node in
    NodeRelationshipCache. This will get the best of both the previous strategy
    of always importing all relationships in one round AND the current strategy
    of importing one type per round (to reduce memory consumption).
    Now the amount of available memory will decide how many rounds of relationship
    imports are required.

    This also removes the per-type-splitting on-disk caching of the input data
    which was done when importing relationships of the first (and biggest) relationship type,
    something which in most case will improve performance of the import in general
    and avoid sections of relationship import where seemingly there were no progress,
    due to only caching relationships.

    There's now an additional setting for how much memory the importer can use
    as a whole and is by default based on amount of free physical memory on the machine.

    The defragmentation of relationship groups after relationship import also makes
    use of available memory and can reduce number of rounds needed.