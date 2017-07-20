<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugins\Feedback;
use Piwik\Common;
use Piwik\Config;
use Piwik\IP;
use Piwik\Mail;
use Piwik\Piwik;
use Piwik\Url;
use Piwik\Version;

/**
 * API for plugin Feedback
 *
 * @method static \Piwik\Plugins\Feedback\API getInstance()
 */
class API extends \Piwik\Plugin\API
{
    /**
     * Sends feedback for a specific feature to the Piwik team or alternatively to the email address configured in the
     * config: "feedback_email_address".
     *
     * @param string   $featureName  The name of a feature you want to give feedback to.
     * @param bool|int $like         Whether you like the feature or not
     * @param string   $message      A message containing the actual feedback
     */
    public function sendFeedbackForFeature($featureName, $like, $message)
    {
        Piwik::checkUserIsNotAnonymous();
        Piwik::checkUserHasSomeViewAccess();

        $translationKeyForFeature = $this->findTranslationKeyForFeatureName($featureName);

        if (empty($translationKeyForFeature)) {
            $translationKeyForFeature = $featureName;
        }

        $likeText = 'Yes';
        if (empty($like)) {
            $likeText = 'No';
        }

        $body = sprintf("Feature: %s\nLike: %s\nFeedback:\n%s\n", $translationKeyForFeature, $likeText, $message);

        $this->sendMail($featureName, $body);
    }

    private function sendMail($name, $body)
    {
        $feedbackEmailAddress = Config::getInstance()->General['feedback_email_address'];

        $subject = '[ Feedback Feature - Piwik ] ' . $name;
        $body    = Common::unsanitizeInputValue($body) . "\n"
                 . 'Piwik ' . Version::VERSION . "\n"
                 . 'IP: ' . IP::getIpFromHeader() . "\n"
                 . 'URL: ' . Url::getReferrer() . "\n";

        $mail = new Mail();
        $mail->setFrom(Piwik::getCurrentUserEmail());
        $mail->addTo($feedbackEmailAddress, 'Piwik Team');
        $mail->setSubject($subject);
        $mail->setBodyText($body);
        @$mail->send();
    }

    private function findTranslationKeyForFeatureName($featureName)
    {
        if (empty($GLOBALS['Piwik_translations'])) {
            return;
        }

        foreach ($GLOBALS['Piwik_translations'] as $key => $translations) {
            $possibleKey = array_search($featureName, $translations);
            if (!empty($possibleKey)) {
                return $key . '_' . $possibleKey;
            }
        }
    }
}