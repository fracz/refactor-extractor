commit fdb1267cb1e4586b4f7f728c82f69834227652ee
Author: Rob Bayer <rob.bayer@gmail.com>
Date:   Mon Mar 5 12:04:34 2012 -0800

    Add remote IMAP search support.

    * rbayer/IMAPsearch: (21 commits)
      More cleanup
      Code Cleanup getRemoteSearchFullText -> isRemoteSearchFullText line wraps for preference items
      Refactor to allow fetching of extra search results beyond original request.  Most code moved out of ImapStore and ImapFolder and into MessagingController.searchRemoteMessagesSynchronous.  Should make it easier to add remoteSearch for other server types.
      Prevent delete of search results while search results open
      remove duplicated code block
      Don't hide Crypto when IMAPsearch disabled
      Code Style Cleanup: Tabs -> 4 spaces Remove trailing whitespace from blank lines
      tabs -> spaces (my bad...)
      Fix opening of folders to be Read-Write when necessary, even if they were previously opened Read-Only.
      add missing file
      Working IMAP search, with passable UI.
      UI improvements
      Simple help info when enabling Remote Search
      Dependency for preferences
      Basic IMAP search working