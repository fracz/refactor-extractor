commit ab65c797bf1a66fe0710af06bd6add5ef94d2809
Author: Kun Zhang <zhangkun83@users.noreply.github.com>
Date:   Wed Aug 23 11:24:56 2017 -0700

    grpclb: refactor main GRPCLB logic out of GrpclbLoadBalancer. (#3369)

    GrpclbLoadBalancer can work in non-GRPCLB (delegate) mode according to
    name resolution results.  Previously the policy selection, delegation
    and GRPCLB logic are in the same file, which is not very readable.  It
    will get worse as we going to implement policy fallback logic soon.
    This PR refactors the GRPCLB logic out, and makes GrpclbLoadBalancer
    focus on the policy selection and delegation logic.