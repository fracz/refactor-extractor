commit a60e212d0dda7d2a748180ce77405f2463c9cf53
Author: Eric Laurent <elaurent@google.com>
Date:   Tue Dec 28 16:49:07 2010 -0800

    Fix issue 3261656.

    The problem can occur if a sample is started at the same time as the last AudioTrack callback
    for a playing sample is called. At this time, allocateChannel() can be called concurrently with moveToFront()
    which can cause an entry in mChannels being used by moveToFront() to be erased temporarily by allocateChannel().

    The fix consists in making sure that the SoundPool mutex is held whenever play(), stop() or done() are called.

    In addition, other potential weaknesses have been removed by making sure that the channel mutex is held while
    starting, stopping and processing the AudioTrack call back.

    To that purpose, a mechanism similar to the channel restart method is implemented to avoid stopping channels
    from the AudioTrack call back but do it from the restart thread instead.

    The sound effects SounPool management in AudioService has also been improved to make sure that the samples have
    been loaded when a playback request is received and also to immediately release the SoundPool when the effects are
    unloaded without waiting for the GC to occur.
    The SoundPool.java class was modified to allow the use of a looper attached to the thread in which the sample
    loaded listener is running and not to the thread in which the SoundPool is created.

    The maximum number of samples that can be loaded in a SoundPool lifetime as been increased from 255 to 65535.

    Change-Id: I368a3bdfda4239f807f857c3e97b70f6b31b0af3