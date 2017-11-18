commit eb26e6263ab9ef7f659efa4e7977161d014c5ff2
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Feb 6 17:45:59 2012 +0100

    Housekeeping. Made sure the Jvm returned by Jvm.forHome() does expose only the information it knows, e.g. what is declared in the new interface. Later on, we can do some more refactoring in Jvm (e.g. replace condidtionals with different implementations, etc.)