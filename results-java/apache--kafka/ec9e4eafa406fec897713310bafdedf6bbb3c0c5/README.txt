commit ec9e4eafa406fec897713310bafdedf6bbb3c0c5
Author: Eno Thereska <eno.thereska@gmail.com>
Date:   Wed May 3 16:15:54 2017 -0700

    KAFKA-5045: Clarify on KTable APIs for queryable stores

    This is the implementation of KIP-114: KTable state stores and improved semantics:
    - Allow for decoupling between querying and materialisation
    - consistent APIs, overloads with queryableName and without
    - depreciated several KTable calls
    - new unit and integration tests

    In this implementation, state stores are materialized if the user desires them to be queryable. In subsequent versions we can offer a second option, to have a view-like state store. The tradeoff then would be between storage space (materialize) and re-computation (view). That tradeoff can be exploited by later query optimizers.

    Author: Eno Thereska <eno.thereska@gmail.com>

    Reviewers: Damian Guy, Matthias J. Sax, Guozhang Wang

    Closes #2832 from enothereska/KAFKA-5045-ktable