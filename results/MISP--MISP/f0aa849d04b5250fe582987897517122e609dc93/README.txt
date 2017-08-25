commit f0aa849d04b5250fe582987897517122e609dc93
Author: iglocska <andras.iklody@gmail.com>
Date:   Tue Feb 3 17:06:05 2015 +0100

    Various improvements to the exports

    - Unified the way exports accept negated parameters
    - Fixed the documentation
    - Most exports are now restrictable by the event date (From/To parameters)
    - none cached XML export now writes to file after converting each event, clearing the memory and resolving any potential memory issues