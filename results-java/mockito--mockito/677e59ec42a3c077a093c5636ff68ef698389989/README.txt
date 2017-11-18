commit 677e59ec42a3c077a093c5636ff68ef698389989
Author: Christian Schwarz <chriss.dev@github>
Date:   Fri Nov 25 15:22:51 2016 +0100

    ValuePrinter small improvements
     * added private construtor
     * removed unneccessary 'else' statements
     * added missing type arguments
     * removed handling of a null Iterator in printValues(), if null is
    passed a NPE is thrown now to indicate a bug