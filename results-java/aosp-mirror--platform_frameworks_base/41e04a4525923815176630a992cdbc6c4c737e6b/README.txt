commit 41e04a4525923815176630a992cdbc6c4c737e6b
Author: Chris Thornton <thorntonc@google.com>
Date:   Mon Apr 18 16:51:08 2016 -0700

    Use .equals() to compare two UUIDs in SoundTriggerHelper, rather than ==

    Also refactors ModelData::clearState to clear a bit more of the state.

    Bug:28251543
    Change-Id: I18d7ccd90a6a9ee8bc8743d9a92c48f17d87c842