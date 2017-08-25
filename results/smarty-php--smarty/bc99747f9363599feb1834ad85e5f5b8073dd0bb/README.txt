commit bc99747f9363599feb1834ad85e5f5b8073dd0bb
Author: Uwe Tews <uwe.tews@googlemail.com>
Date:   Tue Jul 7 02:01:45 2015 +0200

    - improvement allow fetch() or display() called on a template object to get output from other template
         like $template->fetch('foo.tpl') https://github.com/smarty-php/smarty/issues/70