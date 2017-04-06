commit d24600830bc8b76ab15d0105c048885b582fc13a
Author: Shay Banon <kimchy@gmail.com>
Date:   Wed Mar 19 13:44:44 2014 +0100

    Use BytesReference to write to translog files
    Instead of using byte arrays, pass the BytesReference to the actual translog file, and use the new copyTo(channel) method to write. This will improve by not potentially having to convert the data to a byte array
    closes #5463