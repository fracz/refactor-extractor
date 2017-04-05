commit 882b075fdfe37af527aadcc14832ba4ada3b5d74
Author: Violeta Georgieva <vgeorgieva@pivotal.io>
Date:   Tue Oct 18 13:14:55 2016 +0300

    DefaultDataBuffer improvements

    DefaultDataBuffer.grow(int):
    - Copy only the remaining data
    - Update readPosition/writePosition