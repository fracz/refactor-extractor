commit 84efa099ab968775ce25ff3e55af81802a964010
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Nov 20 17:43:35 2014 +0100

    Tidy-up in buildSrc - pushed out logic that ignores commits

    In order to improve clarity of the impl. Reinvented the wheel with Predicate interface (I don't want external dependencies for now).