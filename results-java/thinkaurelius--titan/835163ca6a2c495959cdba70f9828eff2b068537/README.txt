commit 835163ca6a2c495959cdba70f9828eff2b068537
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Jan 17 17:43:30 2014 -0500

    Early implementation of expired lock cleaner

    * Renamed class ExpiredLockDeleter to ExpiredLockCleaner

    * Extracted ConsistentKeyLocker column constants
      ByteBufferUtil.oneBuffer(9) and ByteBufferUtil.zeroBuffer(9) into
      public static finals; ConsistentKeyLockerTest and ExpiredLockCleaner
      now use them

    * Still lots of optimization/refactoring potential in
      ExpiredLockCleaner and areas for added coverage in its test

    * Still need to create an executor into which CKL can submit
      ExpiredLockCleaner instances; as of this commit, ELC is only used by
      its test and is never actually used by Titan in ordinary operation