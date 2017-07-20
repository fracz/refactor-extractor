<?php

namespace Wallabag\CoreBundle\Tests\Controller;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class WallabagRestControllerTest extends WebTestCase
{
    /**
     * Generate HTTP headers for authenticate user on API
     *
     * @param $username
     * @param $password
     * @param $salt
     *
     * @return array
     */
    private function generateHeaders($username, $password, $salt)
    {
        $encryptedPassword = sha1($password.$username.$salt);
        $nonce = substr(md5(uniqid('nonce_', true)), 0, 16);

        $now = new \DateTime('now', new \DateTimeZone('UTC'));
        $created = (string) $now->format('Y-m-d\TH:i:s\Z');
        $digest = base64_encode(sha1(base64_decode($nonce).$created.$encryptedPassword, true));

        return array(
            'PHP_AUTH_USER' => 'username',
            'HTTP_AUTHORIZATION' => 'Authorization profile="UsernameToken"',
            'HTTP_x-wsse' => 'X-WSSE: UsernameToken Username="'.$username.'", PasswordDigest="'.$digest.'", Nonce="'.$nonce.'", Created="'.$created.'"',
        );
    }

    public function testGetSalt()
    {
        $client = $this->createClient();
        $client->request('GET', '/api/salts/admin.json');
        $this->assertEquals(200, $client->getResponse()->getStatusCode());

        $client->request('GET', '/api/salts/notfound.json');
        $this->assertEquals(404, $client->getResponse()->getStatusCode());
    }

    public function testGetOneEntry()
    {
        $client = $this->createClient();
        $client->request('GET', '/api/salts/admin.json');
        $content = json_decode($client->getResponse()->getContent());

        $headers = $this->generateHeaders('admin', 'test', $content[0]);

        $client->request('GET', '/api/entries/1.json', array(), array(), $headers);
        $this->assertContains('This is my content', $client->getResponse()->getContent());

        $this->assertTrue(
            $client->getResponse()->headers->contains(
                'Content-Type',
                'application/json'
            )
        );
    }

    public function testGetEntries()
    {
        $client = $this->createClient();
        $client->request('GET', '/api/salts/admin.json');
        $content = json_decode($client->getResponse()->getContent());

        $headers = $this->generateHeaders('admin', 'test', $content[0]);

        $client->request('GET', '/api/entries', array(), array(), $headers);
        $this->assertContains('Mailjet', $client->getResponse()->getContent());

        $this->assertTrue(
            $client->getResponse()->headers->contains(
                'Content-Type',
                'application/json'
            )
        );
    }
}