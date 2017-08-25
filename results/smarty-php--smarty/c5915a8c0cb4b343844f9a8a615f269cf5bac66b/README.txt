commit c5915a8c0cb4b343844f9a8a615f269cf5bac66b
Author: Uwe Tews <uwe.tews@googlemail.com>
Date:   Tue May 5 00:41:30 2015 +0200

    - improvement use is_file() checks to avoid errors suppressed by @ which could still cause problems (https://github.com/smarty-php/smarty/issues/24)