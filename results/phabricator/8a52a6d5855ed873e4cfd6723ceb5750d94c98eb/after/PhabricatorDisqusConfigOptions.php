<?php

final class PhabricatorDisqusConfigOptions
  extends PhabricatorApplicationConfigOptions {

  public function getName() {
    return pht("Integration with Disqus");
  }

  public function getDescription() {
    return pht("Disqus authentication and integration options.");
  }

  public function getOptions() {
    return array(
      $this->newOption('disqus.auth-enabled', 'bool', false)
        ->setOptions(
          array(
            pht("Disable Disqus Authentication"),
            pht("Enable Disqus Authentication"),
          ))
        ->setDescription(
          pht(
            'Allow users to login to Phabricator using Disqus credentials.')),
      $this->newOption('disqus.registration-enabled', 'bool', true)
        ->setOptions(
          array(
            pht("Disable Disqus Registration"),
            pht("Enable Disqus Registration"),
          ))
        ->setDescription(
          pht(
            'Allow users to create new Phabricator accounts using Disqus '.
            'credentials.')),
      $this->newOption('disqus.auth-permanent', 'bool', false)
        ->setOptions(
          array(
            pht("Allow Disqus Account Unlinking"),
            pht("Permanently Bind Disqus Accounts"),
          ))
        ->setDescription(
          pht(
            'Are Phabricator accounts permanently bound to Disqus '.
            'accounts?')),
      $this->newOption('disqus.application-id', 'string', null)
        ->setDescription(
          pht(
            'Disqus "Client ID" to use for Disqus API access.')),
      $this->newOption('disqus.application-secret', 'string', null)
        ->setDescription(
          pht(
            'Disqus "Secret" to use for Diqsus API access.')),
      $this->newOption('disqus.shortname', 'string', null)
        ->setSummary(pht("Shortname for Disqus comment widget."))
        ->setDescription(
          pht(
            "Website shortname to use for Disqus comment widget in Phame. ".
            "For more information, see:\n\n".
            "[[http://docs.disqus.com/help/4/ | Disqus Quick Start Guide]]\n".
            "[[http://docs.disqus.com/help/68/ | Information on Shortnames]]")),
    );
  }

}