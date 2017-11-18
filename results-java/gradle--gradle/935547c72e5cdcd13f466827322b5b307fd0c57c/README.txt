commit 935547c72e5cdcd13f466827322b5b307fd0c57c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Dec 19 23:50:28 2011 +0100

    Tooling api provides jdk and jvm args information…

    First stab at the implementation, to spark the conversation. Please give feedback as there are multiple ways of solving this use case.

    -I went for the simplest solution: If the client asks for BuildEnvironment model I'm short-circuiting in DefaultConnection and I'm returning the information. E.g. the information about the build environment is available in the provider without the need to launch gradle or talk to the daemon.
    -Alternative approaches include asking daemon for its build environment or implementing the logic in IDE model builders, e.g. where all other model builders live at the moment.
    -some refactorings are pending
    -BuildEnvironment model interface must be reviewed. It's implemented in the simplest possible way at the moment.
    -the integration coverage needs better assertions but they should be easier to implement once the next story is played (e.g. adding jvm args / java home configurability to the tooling api.