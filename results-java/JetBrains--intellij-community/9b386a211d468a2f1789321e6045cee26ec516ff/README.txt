commit 9b386a211d468a2f1789321e6045cee26ec516ff
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Fri Oct 27 16:16:06 2017 +0700

    DFA: refactor nullability problems for stricter type checking

    NullabilityProblemKind is introduced which is parameterized by anchor type. A NullabilityProblem now represents a pair of problem kind and anchor bound by a type variable.