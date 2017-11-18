commit 71886678d4b6a71ce9bf01a768d7615bf394ce4d
Author: Laszlo Csomor <laszlocsomor@google.com>
Date:   Wed Oct 25 17:48:07 2017 +0200

    Windows,JNI,logging: improve error messages

    In this commit:
    - introduce the MakeErrorMessage function, which
      creates a structured error message with file
      and line information of the error's origin
    - update all error messages in the Windows JNI
      library
    - simplify GetLastErrorMessage to just convert an
      error code to string, without prepending a cause

    Change-Id: Ia8162bfdaee37d4b7ccb3a46d6c8a861b0a1bd94
    PiperOrigin-RevId: 173402968