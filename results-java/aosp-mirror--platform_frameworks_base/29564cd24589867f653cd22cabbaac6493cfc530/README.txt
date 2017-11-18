commit 29564cd24589867f653cd22cabbaac6493cfc530
Author: Narayan Kamath <narayan@google.com>
Date:   Thu Aug 7 10:57:40 2014 +0100

    Remove system_server classes from the boot image.

    We set the system_server classpath in the environment
    (like we do with BOOTCLASSPATH). After the zygote forks
    the system_server, we dexopt the classpath (if needed)
    and then launch the system server with the correct
    PathClassLoader. This needed several small / medium
    refactorings :

    - The logic for connecting to installd is now in a separate
      class and belongs in the system_server.
    - SystemService / SystemServiceManager have now moved to
      classes.jar. They are only used from there, and since they
      use Class.forName, we want them to be loaded by the
      system_server classloader, and not the bootclassloader.
    - BootReceiver now moves to frameworks.jar, because it is
      used by ActivityThread and friends.

    bug: 16555230

    Change-Id: Ic84f0b2baf611eeedff6d123cb7191bb0259e600