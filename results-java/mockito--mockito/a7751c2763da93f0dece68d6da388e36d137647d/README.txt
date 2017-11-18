commit a7751c2763da93f0dece68d6da388e36d137647d
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Apr 7 23:20:31 2013 +0200

    Changed the vargars behavior of argument captor. I prefer if the captor handles the varargs seamlessly, e.g. without any extra methods. The change needs some refactoring and documentation. The previous implementation was nice and had nice coverage - we might want to put that back if our users request extra methods for retrieving varg values from the captor.