commit 4dd351fad3c847eb3e1a9afe8549edc4efb36303
Author: Andy Fragen <andy@thefragens.com>
Date:   Tue Nov 8 13:22:11 2016 -0800

    refactor rollback/branch switch transient set

    using the 'wp_get_update_data' hook should put an end to the 'plugin or theme is up to date message during a rollback or branch switch