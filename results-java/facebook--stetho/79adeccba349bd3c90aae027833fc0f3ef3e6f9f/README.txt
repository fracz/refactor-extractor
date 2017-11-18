commit 79adeccba349bd3c90aae027833fc0f3ef3e6f9f
Author: Josh Guilfoyle <devjasta@fb.com>
Date:   Thu Feb 26 10:54:15 2015 -0800

    Fix visual glitches in DOMStorage UI

    Two separate issues are addressed with this minor refactor:

      1. Maintain a shallow copy of the shared preferences map so that
         edited values can properly deliver domStorageItemUpdated instead of
         ItemRemoved/ItemAdded.  This fixes items being moved to the end of the
         list on update.  This memory is reapable when all peers are
         disconnected.
      2. On setDOMStorageItem failure, force refresh the UI state so that it
         stays in sync with the existing values.  The UI is optimistic and will
         assume success in setDOMStorageItem otherwise.