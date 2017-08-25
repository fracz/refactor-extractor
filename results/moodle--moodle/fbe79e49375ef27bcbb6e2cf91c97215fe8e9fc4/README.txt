commit fbe79e49375ef27bcbb6e2cf91c97215fe8e9fc4
Author: sam marshall <s.marshall@open.ac.uk>
Date:   Tue May 13 18:43:21 2014 +0100

    MDL-45535 CSS chunking breaks media rules if CSS invalid (IE)

    Invalid CSS with extra } symbols, which previously (by fluke)
    worked, was broken by recent chunking improvements. This change
    makes the chunking code robust against this (stupid) situation.