commit 0d007ff5c17b17bbfa2bdb0704927c9a31d5d0ac
Author: ulfjack <ulfjack@google.com>
Date:   Tue Jun 27 13:27:16 2017 +0200

    Simplify the RemoteActionCache interface

    - merge all the inputs upload functionality into a single ensureInputsPresent
      method
    - merge all of the action result upload functionality into a single upload
      method
    - merge all the download functionality into a single download method

    This significantly simplifies the caller of this interface, and opens the door
    for additional performance improvements in implementations which now have more
    control over the upload / download flows; in particular, in the gRPC case, we
    can upload stdout / stderr using the existing chunker - upload of stdout /
    stderr is no longer serialized with file upload.

    In particular, the CachedLocalSpawnRunner test becomes much simpler, since it
    no longer needs to handle the previous more complex upload code path.

    PiperOrigin-RevId: 160260161