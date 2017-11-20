commit 8ef199d042fad9e52459a4dfe44b910a46307bd9
Author: Tomasz Setkowski <tom@ai-traders.com>
Date:   Sun Jun 28 17:05:17 2015 +0200

    #1133 ConfigMaterialUpdaterIntegrationTest - parse valid config from repo test case

    HgTestRepo is refactored to be have more public methods - we care about
    content of commited files now.
    Fixed ConfigRepoConfig to use fingerprint instead of equals for material
    equality.