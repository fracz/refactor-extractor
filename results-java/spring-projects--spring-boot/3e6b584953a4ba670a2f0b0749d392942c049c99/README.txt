commit 3e6b584953a4ba670a2f0b0749d392942c049c99
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Thu Mar 10 14:42:48 2016 +0100

    Add Date as a support property type

    Rather than exposing a raw String with the epoch time, GitProperties
    now exposes the actual `java.util.Date`. `InfoProperties` has been
    improved to return such data type when the raw value is an epoch time.