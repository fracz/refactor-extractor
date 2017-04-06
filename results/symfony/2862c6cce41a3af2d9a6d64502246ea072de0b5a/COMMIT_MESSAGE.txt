commit 2862c6cce41a3af2d9a6d64502246ea072de0b5a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 20 21:01:41 2010 +0200

    refactored configuration names

    How to upgrade (have a look at the skeleton):

      * the "web:config" namespace is now "app:config"

          -    <web:config csrf-secret="xxxxxxxxxx" charset="UTF-8" error-handler="null">
          -        <web:router resource="%kernel.root_dir%/config/routing.xml" />
          -        <web:validation enabled="true" annotations="true" />
          -    </web:config>
          +    <app:config csrf-secret="xxxxxxxxxx" charset="UTF-8" error-handler="null">
          +        <app:router resource="%kernel.root_dir%/config/routing.xml" />
          +        <app:validation enabled="true" annotations="true" />
          +    </app:config>

      * the "web:templating" namespace is now a sub-namespace of "app:config"

          -    <web:templating
          -        escaping="htmlspecialchars"
          -    />
          +    <app:config>
          +        <app:templating escaping="htmlspecialchars" />
          +    </app:config>

      * the "web:user" namespace is now a sub-namespace of "app:config"

          -    <web:user default-locale="fr">
          -        <web:session name="SYMFONY" type="Native" lifetime="3600" />
          -    </web:user>
          +    <app:config>
          +        <app:user default-locale="fr">
          +            <app:session name="SYMFONY" type="Native" lifetime="3600" />
          +        </app:user>
          +    </app:config>

      * the "web:test" namespace is now a sub-namespace of "app:config"

          -    <web:test />
          +    <app:config error_handler="false">
          +        <app:test />
          +    </app:config>

      * the "swift:mailer" namespace is now "swiftmailer:config"

          -    <swift:mailer
          +    <swiftmailer:config
                   transport="smtp"
                   encryption="ssl"
                   auth_mode="login"

      * the "zend:logger" namespace is now a sub-namespace of "zend:config"

          -    <zend:logger
          -        priority="info"
          -        path="%kernel.logs_dir%/%kernel.environment%.log"
          -    />
          +    <zend:config>
          +        <zend:logger priority="info" path="%kernel.logs_dir%/%kernel.environment%.log" />
          +    </zend:config>