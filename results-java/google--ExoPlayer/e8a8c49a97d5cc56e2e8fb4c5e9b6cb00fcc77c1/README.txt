commit e8a8c49a97d5cc56e2e8fb4c5e9b6cb00fcc77c1
Author: Martin Bonnin <martin.bonnin@dailymotion.com>
Date:   Tue Dec 23 13:47:50 2014 +0100

    better handling of input format change for non-adaptive codecs

    * this fixes a bug when switching from HE-AAC 22050Hz to AAC 44100Hz (the AudioTrack was not reset and we were trying to send a bad number of bytes, triggering a "AudioTrack.write() called with invalid size" error)
    * this also improves quality switches, making it almost seamless