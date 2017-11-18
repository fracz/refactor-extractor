commit de23f5636010143938e143809b70c28ab15aa1ef
Author: Jean-Michel Trivi <jmtrivi@google.com>
Date:   Fri Mar 21 09:15:43 2014 -0700

    Begin refactor of MediaFocusControl

    Extract class that handles each entry in the remote control stack
      and move it to another file, MediaController.java.
    Rename RemoteControlStackEntry to MediaController as each instance
      will not just encapsulate information about the corresponding
      (if any) RemoteControlClient.
    This is just a CL for the renaming and extraction into a new file
      of existing code. Obvious required changes are labelled "FIXME".

    Change-Id: Ifbdac1d70e4d279ab175eef03e9d792d44873c51