commit 42a851a3df17cfa8ea0fd81fa0bf4a2af4c1061a
Author: Taylor Otwell <taylor@laravel.com>
Date:   Wed Dec 14 14:00:06 2016 -0600

    Scheduler Improvements (#16806)

    This PR adds improvements to the scheduler. Previously, when ->runInBackground() was used "after" hooks were not run, meaning output was not e-mailed to the developer (when using emailOutputTo.

    This change provides a new, hidden schedule:finish command that is used to fire the after callbacks for a given command by its mutex name. This command will not show in the Artisan console command list since it uses the hidden command feature which is new in Symfony 3.2.