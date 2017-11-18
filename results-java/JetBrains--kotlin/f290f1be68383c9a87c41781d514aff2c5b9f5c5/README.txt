commit f290f1be68383c9a87c41781d514aff2c5b9f5c5
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Jun 21 22:00:31 2016 +0300

    Initial support of type inference for callable references

    There are two main changes here:

    - In CallCompleter, there was a bug: we assumed that the return type of a
      candidate must be a subtype of the expected type and were adding a
      corresponding constraint to the system. However, this is not true for
      callable references where the type of the expression is KFunctionN<...> and
      the return type of the candidate must be a subtype of the _last generic
      argument_ of the functional type.
    - In CandidateResolver, we use a more correct (although still not precise)
      heuristic to determine if a candidate fits based on the non-substituted type
      of the callable reference expression which it would produce.

    This can be further improved, see TODOs in CallCompleter. Also this does not
    influence resolution of callable references being passed as arguments to
    generic calls (that happens in GenericCandidateResolver)

     #KT-10968 Fixed
     #KT-11075 Fixed
     #KT-12286 Fixed
     #KT-12963 Open
     #KT-12964 Open