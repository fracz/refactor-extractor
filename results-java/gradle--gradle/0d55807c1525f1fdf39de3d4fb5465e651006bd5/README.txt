commit 0d55807c1525f1fdf39de3d4fb5465e651006bd5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Apr 23 20:33:15 2012 +0200

    First steps towards providing Gradle module information via the tooling api.

    -Only added to the IDEA model for now. The Eclipse model is a small step ahead.
    -Added some initial capability of providing module version id via the LenientConfiguration. This needs better modelling.
    -A couple of TODOs are left for some deeper analysis and refactoring.