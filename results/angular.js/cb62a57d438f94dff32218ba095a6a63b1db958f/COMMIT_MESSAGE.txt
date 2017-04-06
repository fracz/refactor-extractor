commit cb62a57d438f94dff32218ba095a6a63b1db958f
Author: Robin Böhm <robinboehm@googlemail.com>
Date:   Sun Apr 28 20:05:44 2013 +0200

    refact(ngClass): improve performance through bitwise operations

    Change modulo % 2 operations to bitwise & 1
    Read about this in Nicholas C. Zakas book "High Performance JavaScript"(ISBN: 978-0-596-80279-0)
    Use the Fast Parts --> Bitwise Operators --> Page 156++
    Proven at http://jsperf.com/modulo-vs-bitwise/11