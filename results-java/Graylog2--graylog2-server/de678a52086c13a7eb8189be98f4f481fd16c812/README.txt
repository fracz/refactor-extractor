commit de678a52086c13a7eb8189be98f4f481fd16c812
Author: Bernd Ahlers <bernd@graylog.com>
Date:   Wed Mar 2 16:02:08 2016 +0100

    Some refactoring after feedback

    - Remove builder from RestPermission
    - Rename RestPermissionsPlugin to PluginPermissions
    - Add `@NotBlank` to RestPermission#create()
    - Use Collections#emptySet() instead of new HashSet instance
    - Rename methods in PluginModule and Graylog2Module