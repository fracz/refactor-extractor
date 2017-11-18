commit 266aa36b426eabb4a74f2ced9b45ebe55687d2c4
Author: Yigit Boyar <yboyar@google.com>
Date:   Sun Apr 5 02:09:10 2015 -0700

    Fix ternary handling and generate better code

    This CL fixes an issue about ternary expressions where
    a ternary expression would be evaluated with its last
    evaluated dependency. This would create a problem where
    ternary expressions would not be evaluated if other branch
    of the conditional is chosen, This bug is fixed by checking
    outher flags such that we'll still calculate it together
    if all dependencies are behind the same flag vs we'll
    calculate it independently if its dependency flags are different.

    This CL also improves the generated code in two ways:
      - When there is an if inside if, we don't add flag check (the if)
        if all of its conditions are covered in the parent if.
      - I replaced flag names with binary values. This looks more
        readable then generated names.

    Bug: 20073197
    Change-Id: I9d07868206a5393d6509ab0a205b30a796e11107