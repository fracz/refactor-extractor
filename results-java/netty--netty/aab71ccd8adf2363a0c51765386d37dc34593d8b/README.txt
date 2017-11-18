commit aab71ccd8adf2363a0c51765386d37dc34593d8b
Author: Trustin Lee <trustin@gmail.com>
Date:   Thu Jun 7 17:25:15 2012 +0900

    Remove Channel(Inbound|Outbound)HandlerAdapter which does nothing

    - Thanks to the recent refactoring, Channel(Inbound|Outbound)Handler-
      Adapter ended up having empty body.  No need to keep.