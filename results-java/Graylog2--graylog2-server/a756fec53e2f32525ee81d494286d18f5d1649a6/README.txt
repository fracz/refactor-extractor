commit a756fec53e2f32525ee81d494286d18f5d1649a6
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Tue Nov 18 15:23:21 2014 +0100

    refactor handling the internal codec names to use an annotation

    move annotation classes into separate package
    let MessageInput set the codec's name directly, which further reduces the API of RawMessage that codec authors have to deal with