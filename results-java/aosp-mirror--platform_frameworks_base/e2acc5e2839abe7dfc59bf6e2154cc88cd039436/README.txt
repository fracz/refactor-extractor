commit e2acc5e2839abe7dfc59bf6e2154cc88cd039436
Author: Alan Viverette <alanv@google.com>
Date:   Wed Aug 26 11:17:17 2015 -0400

    Revert "Revert "View measurement optimization""

    Refactors for readability and adds an API >M check to be compatible with
    the LinearLayout fix that also targets API >M.

    This revert commit reverts revert commit
    9d8a230fbd71ac57ef806326f15fa133ba125083
    which originally reverted commit
    9cefbda11ee5308145d58b0b99ced0f66a0b1cf9.

    Change-Id: I587d733abef0b35a1bb14b6272054322494a7cdd