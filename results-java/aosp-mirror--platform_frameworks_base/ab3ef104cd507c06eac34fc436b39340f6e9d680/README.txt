commit ab3ef104cd507c06eac34fc436b39340f6e9d680
Author: Karl Rosaen <krosaen@google.com>
Date:   Wed Jul 15 10:09:56 2009 -0700

    Run background threads associated with filtering at background priority.

    This improves the typing responsiveness in the browser a lot, and should
    help out in all the other cases in the UI where we use a filter thread.