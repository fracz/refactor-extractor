commit ed3032b6b89a9ea72c211b0e973e2d0540bdc86d
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Fri Oct 7 16:49:34 2016 +0200

    Unlock latency improvements

    - Make sure the latency also gets tracked on first unlocked in
    which we cancel the AsyncTask.
    - Also add it for pattern authentication

    Change-Id: Ie1561264b0a3b75c09819ccc6d269e61e367e1be