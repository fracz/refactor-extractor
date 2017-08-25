commit c1187954ce1a0a87ab5c45476b82aaa5ccf06448
Author: WanWizard <wanwizard@exitecms.org>
Date:   Sun Nov 21 16:40:17 2010 +0100

    removed test controllers from app/classes/controller
    fixed session id rotation issues on concurrent requests
    added a factory method to load a named session storage backend (override the default)
    reorganized the session config file to make it more readable and to allow multiple session storage backends to be used