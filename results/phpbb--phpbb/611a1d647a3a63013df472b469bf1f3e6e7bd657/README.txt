commit 611a1d647a3a63013df472b469bf1f3e6e7bd657
Author: Cullen Walsh <ckwalsh@phpbb.com>
Date:   Wed Apr 20 09:46:36 2011 -0700

    [feature/avatars] Refactor avatar's handle_form

    Since it was performing two distinct operations, refactor handle_form
    to separate functions that prepare and process forms.

    PHPBB3-10018