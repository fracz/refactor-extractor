commit d3e8fd80f7922bf31de8e3e23fada47ddbe94110
Author: Alexandr Evstigneev <Alexandr.Evstigneev@jetbrains.com>
Date:   Wed Sep 27 11:52:17 2017 +0300

    Refactored remote SDK creation (changes API)

    Common part:
    - PyCharm's version of SDK editing was refactored, use setEditing() method to switch form to editing mode
    - initSdk() is now invoked from createSdk() method and may throw RemoteSdkException
    - common initSdk() methods moved to superclass
    - RemoteSdkFactory.initSdk() now throws RemoteSdkException
    - Implemented default completeInitialization() empty method.
    - RemoteSdkAdditionalData.completeInitialization now throws RemoteSdkException
    - CreateRemoteSdkForm.createSdkData now throws RemoteSdkException

    Ruby:
    - RubySdkConfigurator propagating possible exceptions using RemoteSdkException

    IDEA-CR-24953