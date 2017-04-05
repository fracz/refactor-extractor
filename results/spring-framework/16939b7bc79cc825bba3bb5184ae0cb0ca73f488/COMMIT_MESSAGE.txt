commit 16939b7bc79cc825bba3bb5184ae0cb0ca73f488
Author: Violeta Georgieva <vgeorgieva@pivotal.io>
Date:   Mon Jul 25 14:57:45 2016 +0300

    AbstractListenerServerHttpResponse improvements

    This commit changes writeWithInternal(Publisher<DataBuffer> body).
    It is implemented as writeAndFlushWith(Mono.just(body)).