commit ced437b08a06248cd7894a83cc56b4cb48523ce4
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Aug 17 11:20:19 2012 +0200

    Dependency report - refactoring - removed the coupling with the toString().

    The algorigthm dependended on the results of toString() from certain objects which was undesired.