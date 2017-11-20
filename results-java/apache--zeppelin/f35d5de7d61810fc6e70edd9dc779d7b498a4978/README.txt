commit f35d5de7d61810fc6e70edd9dc779d7b498a4978
Author: 1ambda <1amb4a@gmail.com>
Date:   Mon Feb 27 17:39:42 2017 +0900

    [ZEPPELIN-2069] Helium Package Configuration

    ### What is this PR for?

    Supporting helium package configurations. I attached screenshots.

    #### Implementation details.

    In case of spell, spell developer can create config spec in their `package.json` and it will be part of `helium.json` which is consumed by Zeppelin.

    ```
      "config": {
        "repeat": {
          "type": "number",
          "description": "How many times to repeat",
          "defaultValue": 1
        }
      },
    ```

    1. Persists conf per `package namepackage version` since each version can require different configs even if they are the same package.
    2. Saves key-value config only. Since config spec (e.g `type`, `desc`, `defaultValue`) can be provided. So it's not efficient save both of them.
    3. Extracts config related functions to `helium.service.js` since it can be used not only in `helium.controller.js` for view but also should be used in `paragraph.controller.js`, `result.controller.js` for executing spell.

    ### What type of PR is it?
    [Feature]

    ### Todos
    * [x] - create config view in `/helium`
    * [x] - persist config per `packageversion`
    * [x] - pass config to spell

    ### What is the Jira issue?

    [ZEPPELIN-2069](https://issues.apache.org/jira/browse/ZEPPELIN-2069)

    ### How should this be tested?

    - Build with examples `mvn clean package -Phelium-dev -Pexamples -DskipTests;`
    - Open `/helium` page
    - Update the `echo-spell` config
    - Execute the spell like the screenshot below. (you don't need to refresh the page, since executing spell will fetch config from server)

    ### Screenshots (if appropriate)

    ![config](https://cloud.githubusercontent.com/assets/4968473/22678867/a66db8ae-ed40-11e6-910b-f81e50a62ba4.gif)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1982 from 1ambda/ZEPPELIN-2069/helium-package-configuration and squashes the following commits:

    dbc4f10 [1ambda] fix: Add getAllPackageInfoWithoutRefresh
    ce5f8c0 [1ambda] fix: Remove version 'local'
    696f7f8 [1ambda] fix: DON'T serialize version field in HeliumPackage
    e599ab9 [1ambda] feat: Close spell config panel after saving
    c9b0145 [1ambda] feat: Make spell execution transactional
    d9e87a8 [1ambda] refactor: Create API call for config
    453016b [1ambda] fix: configExists
    e6d5181 [1ambda] fix: Lint error
    33a2bd8 [1ambda] refactor: HeliumService
    f31bf3c [1ambda] feat: Add disabled class to cfg button while fetching
    76d50ca [1ambda] fix: Use artifact as key of config
    729c5ba [1ambda] fix: Remove digest from para ctrl
    4d3c2c7 [1ambda] feat: Add config to framework, examples
    70ebe29 [1ambda] feat: Pass confs to spell interpret()
    115191e [1ambda] refactor: Extact spell related code to helium
    3aa6c54 [1ambda] feat: Support helium conf in frontend
    dea2929 [1ambda] chore: Add conf to example spells
    6910e97 [1ambda] feat: Support config for helium pkg in backend
    0a0c565 [1ambda] feat: Support config, version field for helium pkg