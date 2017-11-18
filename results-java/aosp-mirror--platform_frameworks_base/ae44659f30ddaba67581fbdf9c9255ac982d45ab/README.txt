commit ae44659f30ddaba67581fbdf9c9255ac982d45ab
Author: Craig Mautner <cmautner@google.com>
Date:   Thu Dec 6 19:05:05 2012 -0800

    Call adjustWallpaperWindowsLocked once per pass.

    Also refactor a few methods and improve logging.

    Change-Id: Ic54a1ff99f6de732b31cda5c06d36e8de01a269c