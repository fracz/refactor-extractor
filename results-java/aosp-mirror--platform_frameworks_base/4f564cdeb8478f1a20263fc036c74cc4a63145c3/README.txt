commit 4f564cdeb8478f1a20263fc036c74cc4a63145c3
Author: Steve Howard <showard@google.com>
Date:   Wed Sep 22 15:57:25 2010 -0700

    Tweaks to download manager API based on API review feedback

    * improved documentation of the default destination on the download
      cache and the concomitant consequences

    * removed NETWORK_WIMAX flag for setting allowed networks

    * changed request headers behavior to support multiple instances of
      the same header (as allowed in the HTTP spec), renamed
      setRequestHeader() to addRequestHeader()

    * accept user-facing strings as CharSequences instead of Strings

    * new convenience methods setDestinationInExternalFilesDir() and
      setDestinationInExternalPublicDir() for setting a destination in
      either shared or app-private external storage directories

    * renamed setMediaType() to setMimeType()

    Change-Id: I8781e2214d939c340209cab917bbbba264ab919c