commit 0402191ace91a783010370249ca017b634518bc0
Author: aquilescanta <aquilescanta@google.com>
Date:   Wed Feb 1 08:01:34 2017 -0800

    Make SeiReader injectable to H26xReaders

    This CL is a no-op refactor but allows defining the outputted
    channels through the TsPayloadReaderFactory.

    Issue:#2161

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=146243736