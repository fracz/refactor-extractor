commit 2176873271fd6a54511acb80e84d38f681096006
Author: Daniel Cohen Gindi <danielgindi@gmail.com>
Date:   Tue Aug 9 16:02:07 2016 +0300

    Avoid creating a new formatter if null

    This can improve performance in certain cases,
      and has the advantage of controlling the "global" default formatter
      that is used for null cases.