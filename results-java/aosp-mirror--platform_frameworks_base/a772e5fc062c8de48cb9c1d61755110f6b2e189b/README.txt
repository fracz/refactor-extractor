commit a772e5fc062c8de48cb9c1d61755110f6b2e189b
Author: Arunesh Mishra <arunesh@google.com>
Date:   Mon Jan 25 10:33:11 2016 -0800

    SoundTrigger API improvements.

    This CL implements the SoundTrigger API improvements as given in b/22860713. Only the java-level
    parts are implemented in this CL.

    Key changes include:

    * Addition of a SoundTriggerManager/SoundTriggerDetector system API to manage
      the sound-trigger based sound models.
    * Addition of a SoundTriggerService service that manages all sound models
      including voice (keyphrase) and sound-trigger based models.
    * Includes logic to write sound-trigger based models to the database.
    * VoiceInteractionManager service now uses SoundTriggerService instead of
      SoundTriggerHelper.

    Bug: 22860713
    Change-Id: I7b5c0ed80702527c4460372efeb5e542d3693a69