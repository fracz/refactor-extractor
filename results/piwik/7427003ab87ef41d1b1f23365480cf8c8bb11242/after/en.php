<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 *
 * @category Piwik_Plugins
 * @package Piwik_MobileMessaging
 */

$translations = array(
	'MobileMessaging_Exception_UnknownProvider' => 'Provider name \'%s\' unknown. Try any of the following instead: %s.',
	'MobileMessaging_PluginDescription' => 'Create and download custom SMS reports and have them sent to your mobile daily, weekly or monthly.',
	'MobileMessaging_TopMenu' => 'Email & SMS Reports',
	'MobileMessaging_SettingsMenu' => 'Mobile Messaging',
	'MobileMessaging_Settings_SuperAdmin' => 'Super Admin Settings',
	'MobileMessaging_VerificationText' => 'Code is %s. To validate your phone number and receive Piwik SMS reports please copy this code in the form accessible via Piwik > %s > %s.',
	'MobileMessaging_Settings_LetUsersManageAPICredential' => 'Allow users to manage their own SMS API credential',
	'MobileMessaging_Settings_LetUsersManageAPICredential_No_Help' => 'All users are able to receive SMS Reports and will use your account\'s credits.',
	'MobileMessaging_Settings_SMSProvider' => 'SMS Provider',
	'MobileMessaging_Settings_SMSAPIAccount' => 'Manage SMS API Account',
	'MobileMessaging_Settings_PhoneNumbers' => 'Phone Numbers',
	'MobileMessaging_Settings_PhoneNumbers_Help' => 'Before receiving SMS (text messages) reports on a phone, the phone number must be entered below. <br/> When you click "Add", a SMS containing a code will be sent to the phone. The user will then be able to validate the phone number if he has access to Piwik.',
	'MobileMessaging_Settings_PhoneNumbers_CountryCode_Help' => 'If you do not know the phone country code, look for your country here',
	'MobileMessaging_Settings_PhoneNumbers_Add' => 'Add a new Phone Number',
	'MobileMessaging_Settings_VerificationCodeJustSent' => 'We just sent a SMS to this number with a code: please enter this code above and click "Validate".',
	'MobileMessaging_Settings_PhoneActivated' => 'Phone number validated! You can now receive SMS with your stats.',
	'MobileMessaging_Settings_InvalidActivationCode' => 'Code entered was not valid, please try again.',
	'MobileMessaging_Settings_CountryCode' => 'Country Code',
	'MobileMessaging_Settings_PhoneNumber' => 'Phone Number',
	'MobileMessaging_Settings_ManagePhoneNumbers' => 'Manage Phone Numbers',
	'MobileMessaging_Settings_PleaseSignUp' => 'To create SMS reports and receive short text messages with your websites\' stats on your mobile phone, please sign up with the SMS API and enter your information below.',
	'MobileMessaging_Settings_APIKey' => 'API Key',
	'MobileMessaging_Settings_AddPhoneNumber' => 'Add',
	'MobileMessaging_Settings_ValidatePhoneNumber' => 'Validate',
	'MobileMessaging_Settings_RemovePhoneNumber' => 'Remove',
	'MobileMessaging_Settings_SuspiciousPhoneNumber' => 'If you don\'t receive the text message, you may try without the leading zero. ie. "53948298"',
	'MobileMessaging_Settings_CredentialNotProvidedByAdmin' => 'Before you can create and manage phone numbers, please ask your administrator to connect Piwik to a SMS Account.',
	'MobileMessaging_Settings_CredentialNotProvided' => 'Before you can create and manage phone numbers, please connect Piwik to your SMS Account above.',
	'MobileMessaging_Settings_CredentialProvided' => 'Your %s SMS API account is correctly configured!',
	'MobileMessaging_Settings_UpdateOrDeleteAccount' => 'You can also %supdate%s or %sdelete%s this account.',
	'MobileMessaging_Settings_DeleteAccountConfirm' => 'Are you sure you want to delete the SMS account?',
	'MobileMessaging_MobileReport_PhoneNumbers' => 'Phone Numbers',
	'MobileMessaging_MobileReport_NoPhoneNumbers' => 'Please activate at least one phone number by accessing',
	'MobileMessaging_MobileReport_MobileMessagingSettingsLink' => 'the Mobile Messaging settings page',
	'MobileMessaging_MobileReport_AdditionalPhoneNumbers' => 'You can add more phone numbers by accessing',
	'MobileMessaging_SMS_Content_Too_Long' => '[too long]',
	'MobileMessaging_MultiSites_Must_Be_Activated' => 'To generate SMS texts of your website stats, please enable the plugin MultiSites in Piwik.',
	'MobileMessaging_SMSProvider_Description_Clockwork' => 'You can use Clockwork.com to send SMS Reports from Piwik.<br/> * First, get an API Key form <a href="http://clockworksms.com/platforms/piwik/">Clockwork</a> (Signup is free with some test messages) <br/> * Put your Clockwork API Key in Piwik. <br/> About Clockwork <br/> * Clockwork gives you fast, reliable high quality worldwide SMS delivery, over 450 networks in every corner of the globe. <br/>  * Cost per SMS message is circa ~0.08USD (0.06EUR). </br>  * Most countries and networks are supported but we suggest you check the latest position on their coverage map here <a href="www.clockworksms.com/sms-coverage/">www.clockworksms.com/sms-coverage/</a>',
);