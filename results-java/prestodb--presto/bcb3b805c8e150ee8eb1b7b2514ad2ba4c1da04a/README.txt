commit bcb3b805c8e150ee8eb1b7b2514ad2ba4c1da04a
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Fri Feb 5 14:35:08 2016 +0100

    Remove unspecified values support from TypeCalculation

    All the variables which are involved in calculation must be explicitly declared.
    After Signature binding refactor there will be no such cases when we need to handle undeclared variables in TypeCalculation.