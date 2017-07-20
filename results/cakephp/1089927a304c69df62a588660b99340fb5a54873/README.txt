commit 1089927a304c69df62a588660b99340fb5a54873
Author: Luan Hospodarsky <luan.handrade@gmail.com>
Date:   Sat Oct 17 18:03:42 2015 -0300

    Implemented public methods: link, unlink in HasMany association. A bit of refactory was necessary in _unlinkAssociated method, separating the parts of the code responsible to build the exclusion list and the part responsible to actually unlink. This last part is now present in _unlink method. This method is used when unlinking all or unlinking some associated objects.