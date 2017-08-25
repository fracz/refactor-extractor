commit 1e15c0fdad65462315daa7d3576dbb170f666479
Author: stronk7 <stronk7>
Date:   Sun Jan 28 23:15:16 2007 +0000

    Added Oracle DIRTY HACK to both rs_fetchXX functions.
    Detecting of EOF in rs_fetch_record()
    Big improvement of rs_fetch_next_record() by using FethRow()
    with speeds near native fields[]. MDL-8134

    Merged from MOODLE_17_STABLE