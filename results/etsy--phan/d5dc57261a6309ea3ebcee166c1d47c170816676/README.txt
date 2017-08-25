commit d5dc57261a6309ea3ebcee166c1d47c170816676
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sat Jun 17 13:43:01 2017 -0700

    Improve inferences on condition visitors(for/while/if)

    Improve undefined variable detection a bit.
    Start analyzing the inside of for/while loops using the loop's condition.
    Better analysis of types withing compound if expressions

    Fixes #747 (More complicated conditionals may still have issues)
    Fixes #477 (Doesn't account for `continue` within a loop, but it's an improvement)
    Fixes #859 (But the condition will start leaking out)