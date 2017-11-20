commit 79b30dc2a485545efd94e6c80e7b6c11aca051d4
Author: Marvin S. Addison <marvin.addison@gmail.com>
Date:   Mon Apr 23 16:06:29 2012 -0400

    CAS-1116
    MemCachedTicketRegistry improvements:
     - Add constructor that takes MemcachedClient to expose full range of configuration options
     - Remove support for async writes since it can lead to inconsistent state
     - Improve logging