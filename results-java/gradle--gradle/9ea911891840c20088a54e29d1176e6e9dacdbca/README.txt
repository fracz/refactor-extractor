commit 9ea911891840c20088a54e29d1176e6e9dacdbca
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Thu Sep 27 01:24:51 2012 +0200

    fixed problem where option class would only behave correctly if it directly inherited from AbstractOptions

    - solves a problem with recent GroovyForkOptions refactoring