commit 4d1e2bf6dfb46eb0bc86d9bf674e759c9a31cac5
Author: Andy Fragen <andy@thefragens.com>
Date:   Mon Oct 17 12:02:36 2016 -0700

    Squashed commit of develop:

    commit e12993e54b03c62e94e3c9305eba5744f64f3fe7
    Author: Andy Fragen <andy@thefragens.com>
    Date:   Mon Oct 17 11:52:05 2016 -0700

        cleanup from transient refactoring

    commit 55f9075bcc28a510afd0074e9f6d75602e828593
    Author: Andy Fragen <andy@thefragens.com>
    Date:   Mon Oct 17 09:25:38 2016 -0700

        delete transients using database call

        more complete transient removal. now running in Base.

    commit 38fec8cd3f6186450a7d696e88076903acbed0cc
    Author: Andy Fragen <andy@thefragens.com>
    Date:   Sun Oct 16 20:09:54 2016 -0700

        directly delete all transients

        parameter left key to aid in resetting update_plugins and update_themes transients.

    commit ce8d34351d67b7b191d05b9c841ceeae4d74178a
    Author: Andy Fragen <andy@thefragens.com>
    Date:   Sun Oct 16 17:22:20 2016 -0700

        delete GHU transients when updating GHU

    commit 3183dd315f29de069556dfdd29e8ffa39dc07146
    Author: Andy Fragen <andy@thefragens.com>
    Date:   Sun Oct 16 17:02:04 2016 -0700

        delete transients on deactivation

        :fingers-crossed: