commit d59a5d59df920d743723521a2afed9de1da3373b
Author: Dianne Hackborn <hackbod@google.com>
Date:   Sat Apr 4 14:52:14 2015 -0700

    Various fixes and improvements...

    Issue #19912529: VI: VoiceInteractor callback ClassCastException

    Fix to use correct argument.

    Issue #19912636: VI: Documentation for VoiceInteractionSession.onBackPressed

    Added documentation.

    Issue #19912703: VI: VoiceInteractionSession NPE on Abort Request

    Maybe fix this -- don't crash if there is no active session.

    Issue #19953731: VI: Add value index to...
    ...android.app.VoiceInteractor.PickOptionRequest.Option

    There is now an optional index integer that can be associated with
    every Option object.

    Issue #19912635: VI: Behavior of startActivity when in voice...
    ...interaction is unexpected

    We now forcibly finish the current voice interaction task whenever
    another activity takes focus from it.

    Issue #20066569: Add API to request heap dumps

    New ActivityManager API to set the pss limit to generate heap
    dumps.

    Also added app ops for assist receiving structure and screenshot
    data, so that we can track when it does these things.

    Change-Id: I688d4ff8f0bd0b8b9e3390a32375b4bb7875c1a1