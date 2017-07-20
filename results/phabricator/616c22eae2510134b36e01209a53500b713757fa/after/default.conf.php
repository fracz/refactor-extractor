<?php

/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

return array(

  // The root URI which Phabricator is installed on.
  // Example: "http://phabricator.example.com/"
  'phabricator.base-uri'        => null,

  // The Conduit URI for API access to this install. Normally this is just
  // the 'base-uri' plus "/api/" (e.g. "http://phabricator.example.com/api/"),
  // but make sure you specify 'https' if you have HTTPS configured.
  'phabricator.conduit-uri'     => null,

  'phabricator.csrf-key'        => '0b7ec0592e0a2829d8b71df2fa269b2c6172eca3',

  'phabricator.version'         => 'UNSTABLE',

  // The default PHID for users who haven't uploaded a profile image. It should
  // be 50x50px.
  'user.default-profile-image-phid' => 'PHID-FILE-f57aaefce707fc4060ef',

// -- Access Control -------------------------------------------------------- //

  // Phabricator users have one of three access levels: "anyone", "verified",
  // or "admin". "anyone" means every user, including users who do not have
  // accounts or are not logged into the system. "verified" is users who have
  // accounts, are logged in, and have satisfied whatever verification steps
  // the configuration requires (e.g., email verification and/or manual
  // approval). "admin" is verified users with the "administrator" flag set.

  // These configuration options control which access level is required to read
  // data from Phabricator (e.g., view revisions and comments in Differential)
  // and write data to Phabricator (e.g., upload files and create diffs). By
  // default they are both set to "verified", meaning only verified user
  // accounts can interact with the system in any meaningful way.

  // If you are configuring an install for an open source project, you may
  // want to reduce the "phabricator.read-access" requirement to "anyone". This
  // will allow anyone to browse Phabricator content, even without logging in.

  // Alternatively, you could raise the "phabricator.write-access" requirement
  // to "admin", effectively creating a read-only install.


  // Controls the minimum access level required to read data from Phabricator
  // (e.g., view revisions in Differential). Allowed values are "anyone",
  // "verified", or "admin". Note that "anyone" includes users who are not
  // logged in! You should leave this at 'verified' unless you want your data
  // to be publicly readable (e.g., you are developing open source software).
  'phabricator.read-access'     => 'verified',

  // Controls the minimum access level required to write data to Phabricator
  // (e.g., create new revisions in Differential). Allowed values are
  // "verified" or "admin". Setting this to "admin" will effectively create a
  // read-only install.
  'phabricator.write-access'    => 'verified',


// -- DarkConsole ----------------------------------------------------------- //

  // DarkConsole is a administrative debugging/profiling tool built into
  // Phabricator. You can leave it disabled unless you're developing against
  // Phabricator.

  // Determines whether or not DarkConsole is available. DarkConsole exposes
  // some data like queries and stack traces, so you should be careful about
  // turning it on in production (although users can not normally see it, even
  // if the deployment configuration enables it).
  'darkconsole.enabled'         => true,

  // Always enable DarkConsole, even for logged out users. This potentially
  // exposes sensitive information to users, so make sure untrusted users can
  // not access an install running in this mode. You should definitely leave
  // this off in production. It is only really useful for using DarkConsole
  // utilties to debug or profile logged-out pages. You must set
  // 'darkconsole.enabled' to use this option.
  'darkconsole.always-on'       => false,


// --  MySQL  --------------------------------------------------------------- //

  // The username to use when connecting to MySQL.
  'mysql.user' => 'root',

  // The password to use when connecting to MySQL.
  'mysql.pass' => '',

  // The MySQL server to connect to.
  'mysql.host' => 'localhost',


// -- Email ----------------------------------------------------------------- //

  // Some Phabricator tools send email notifications, e.g. when Differential
  // revisions are updated or Maniphest tasks are changed. These options allow
  // you to configure how email is delivered.

  // You can test your mail setup by going to "MetaMTA" in the web interface,
  // clicking "Send New Message", and then composing a message.

  // Default address to send mail "From".
  'metamta.default-address'     => 'noreply@example.com',

  // When a user takes an action which generates an email notification (like
  // commenting on a Differential revision), Phabricator can either send that
  // mail "From" the user's email address (like "alincoln@logcabin.com") or
  // "From" the 'metamta.default-address' address. The user experience is
  // generally better if Phabricator uses the user's real address as the "From"
  // since the messages are easier to organize when they appear in mail clients,
  // but this will only work if the server is authorized to send email on behalf
  // of the "From" domain. Practically, this means:
  //    - If you are doing an install for Example Corp and all the users will
  //      have corporate @corp.example.com addresses and any hosts Phabricator
  //      is running on are authorized to send email from corp.example.com,
  //      you can enable this to make the user experience a little better.
  //    - If you are doing an install for an open source project and your
  //      users will be registering via Facebook and using personal email
  //      addresses, you MUST NOT enable this or virtually all of your outgoing
  //      email will vanish into SFP blackholes.
  //    - If your install is anything else, you're much safer leaving this
  //      off since the risk in turning it on is that your outgoing mail will
  //      mostly never arrive.
  'metamta.can-send-as-user'    => false,

  // Adapter class to use to transmit mail to the MTA. The default uses
  // PHPMailerLite, which will invoke PHP's mail() function. This is appropriate
  // if mail() actually works on your host, but if you haven't configured mail
  // it may not be so great. You can also use Amazon SES, by changing this to
  // 'PhabricatorMailImplementationAmazonSESAdapter', signing up for SES, and
  // filling in your 'amazon-ses.access-key' and 'amazon-ses.secret-key' below.
  'metamta.mail-adapter'        =>
    'PhabricatorMailImplementationPHPMailerLiteAdapter',

  // When email is sent, try to hand it off to the MTA immediately. This may
  // be worth disabling if your MTA infrastructure is slow or unreliable. If you
  // disable this option, you must run the 'metamta_mta.php' daemon or mail
  // won't be handed off to the MTA. If you're using Amazon SES it can be a
  // little slugish sometimes so it may be worth disabling this and moving to
  // the daemon after you've got your install up and running. If you have a
  // properly configured local MTA it should not be necessary to disable this.
  'metamta.send-immediately'    => true,

  // If you're using Amazon SES to send email, provide your AWS access key
  // and AWS secret key here. To set up Amazon SES with Phabricator, you need
  // to:
  //  - Make sure 'metamta.mail-adapter' is set to:
  //    "PhabricatorMailImplementationAmazonSESAdapter"
  //  - Make sure 'metamta.can-send-as-user' is false.
  //  - Make sure 'metamta.default-address' is configured to something sensible.
  //  - Make sure 'metamta.default-address' is a validated SES "From" address.
  'amazon-ses.access-key'       =>  null,
  'amazon-ses.secret-key'       =>  null,


// --  Facebook  ------------------------------------------------------------ //

  // Can users use Facebook credentials to login to Phabricator?
  'facebook.auth-enabled'       => false,

  // The Facebook "Application ID" to use for Facebook API access.
  'facebook.application-id'     => null,

  // The Facebook "Application Secret" to use for Facebook API access.
  'facebook.application-secret' => null,


// -- Recaptcha ------------------------------------------------------------- //

  // Is Recaptcha enabled? If disabled, captchas will not appear.
  'recaptcha.enabled'           => false,

  // Your Recaptcha public key, obtained from Recaptcha.
  'recaptcha.public-key'        => null,

  // Your Recaptcha private key, obtained from Recaptcha.
  'recaptcha.private-key'       => null,



);