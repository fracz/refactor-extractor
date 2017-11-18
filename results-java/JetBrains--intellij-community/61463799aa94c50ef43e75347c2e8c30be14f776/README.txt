commit 61463799aa94c50ef43e75347c2e8c30be14f776
Author: Dmitry Cheryasov <Dmitry.Cheryasov@jetbrains.com>
Date:   Mon Apr 27 08:33:14 2009 +0400

    A more complete fix for PY-147. Proposes several ways to import potentially importable names,
           adds necessary import statements, handles name clashes.
           Also, bits of semi-related refactoring here and there.