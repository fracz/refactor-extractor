commit f27cb59c36c81663f7b254d15b86ac4cbc3b06d6
Author: Russell Smith <mr-russ@smith2001.net>
Date:   Thu Jul 11 17:14:46 2013 +1000

    MDL-40585 backup: cache XML parent paths

    For $this->groupedpaths, using a key is faster as there is
    no need to do in_array searches.

    Parent cache allows dirname calls to be substantially reduced.
    2048 was chosen as a cache size as this class operates on chunks
    of the restore at a time, 8k questions produces a parent cache of
    about 500 paths and uses 300K of memory.  Scaling up to 2048 will
    use about 1.2M of RAM for really large restores.  This is acceptable
    for the 48% function call reduction and the 10% runtime improvement
    seen.