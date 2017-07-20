commit 4eefce356d50040e3e001ce2660a71b78d86fba5
Author: Martin Hujer <mhujer@gmail.com>
Date:   Sun Mar 26 18:22:10 2017 +0200

    Optimize UUID string decoding

    I realized, that 30% of the request time in our app is spent by
    hydrating the uuid (We are using Doctrine and in this specific request
    I was accidentally hydrating few thousands entities). Luckily, I
    remembered the #160 and tried to do similar optimization for uuid
    decoding. It resulted in 10-20% performance improvement.