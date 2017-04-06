commit 5bff6e421843dffe9a2a1a856a257398cc3a858e
Author: Isabel Drost-Fromm <isabel.drostfromm@elasticsearch.com>
Date:   Mon Dec 7 17:16:55 2015 +0100

    Refactor FieldSortBuilder

    * adds json parsing,
    * refactors json serialisation,
    * adds writable parsing and serialisation,
    * adds json and writable roundtrip test