commit 0159358a0888b64527cee885ac532b0a37b4bbd4
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Nov 13 10:11:11 2012 +0100

    refactored CSS, images, templates included in the built-in bundles

    The goal is to make things more decoupled and more reusable across
    different bundles.

    There will be a PR for the distribution bundle too to simplify the code
    based on this PR.