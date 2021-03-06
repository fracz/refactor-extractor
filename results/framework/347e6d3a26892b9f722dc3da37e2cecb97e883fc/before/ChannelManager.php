<?php

namespace Illuminate\Notifications;

use InvalidArgumentException;
use Illuminate\Support\Manager;
use Nexmo\Client as NexmoClient;
use GuzzleHttp\Client as HttpClient;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Contracts\Bus\Dispatcher as Bus;
use Nexmo\Client\Credentials\Basic as NexmoCredentials;
use Illuminate\Contracts\Notifications\Factory as FactoryContract;
use Illuminate\Contracts\Notifications\Dispatcher as DispatcherContract;

class ChannelManager extends Manager implements DispatcherContract, FactoryContract
{
    /**
     * The default channels used to deliver messages.
     *
     * @var array
     */
    protected $defaultChannels = ['mail', 'database'];

    /**
     * Send the given notification to the given notifiable entities.
     *
     * @param  \Illuminate\Support\Collection|array  $notifiables
     * @param  mixed  $notification
     * @return void
     */
    public function send($notifiables, $notification)
    {
        if ($notification instanceof ShouldQueue) {
            return $this->queueNotification($notifiables, $notification);
        }

        return $this->sendNow($notifiables, $notification);
    }

    /**
     * Send the given notification immediately.
     *
     * @param  \Illuminate\Support\Collection|array  $notifiables
     * @param  mixed  $notification
     * @return void
     */
    public function sendNow($notifiables, $notification)
    {
        if (! $notification->application) {
            $notification->application(
                $this->app['config']['app.name'],
                $this->app['config']['app.logo']
            );
        }

        foreach ($notifiables as $notifiable) {
            $channels = $notification->via($notifiable);

            if (empty($channels)) {
                continue;
            }

            $this->app->make('events')->fire(
                new Events\NotificationSent($notifiable, $notification)
            );

            foreach ($channels as $channel) {
                $this->driver($channel)->send(collect([$notifiable]), $notification);
            }
        }
    }

    /**
     * Queue the given notification instances.
     *
     * @param  mixed  $notification
     * @param  array[\Illuminate\Notifcations\Channels\Notification]
     * @return void
     */
    protected function queueNotification($notifiables, $notification)
    {
        $this->app->make(Bus::class)->dispatch(
            (new SendQueuedNotifications($notifiables, $notification))
                    ->onConnection($notification->connection)
                    ->onQueue($notification->queue)
                    ->delay($notification->delay)
        );
    }

    /**
     * Get a channel instance.
     *
     * @param  string|null  $name
     * @return mixed
     */
    public function channel($name = null)
    {
        return $this->driver($name);
    }

    /**
     * Create an instance of the database driver.
     *
     * @return \Illuminate\Notifications\Channels\DatabaseChannel
     */
    protected function createDatabaseDriver()
    {
        return $this->app->make(Channels\DatabaseChannel::class);
    }

    /**
     * Create an instance of the broadcast driver.
     *
     * @return \Illuminate\Notifications\Channels\BroadcastChannel
     */
    protected function createBroadcastDriver()
    {
        return $this->app->make(Channels\BroadcastChannel::class);
    }

    /**
     * Create an instance of the mail driver.
     *
     * @return \Illuminate\Notifications\Channels\MailChannel
     */
    protected function createMailDriver()
    {
        return $this->app->make(Channels\MailChannel::class);
    }

    /**
     * Create an instance of the Nexmo driver.
     *
     * @return \Illuminate\Notifications\Channels\NexmoSmsChannel
     */
    protected function createNexmoDriver()
    {
        return new Channels\NexmoSmsChannel(
            new NexmoClient(new NexmoCredentials(
                $this->app['config']['services.nexmo.key'],
                $this->app['config']['services.nexmo.secret']
            )),
            $this->app['config']['services.nexmo.sms_from']
        );
    }

    /**
     * Create an instance of the Slack driver.
     *
     * @return \Illuminate\Notifications\Channels\SlackWebhookChannel
     */
    protected function createSlackDriver()
    {
        return new Channels\SlackWebhookChannel(new HttpClient);
    }

    /**
     * Create a new driver instance.
     *
     * @param  string  $driver
     * @return mixed
     *
     * @throws \InvalidArgumentException
     */
    protected function createDriver($driver)
    {
        try {
            return parent::createDriver($driver);
        } catch (InvalidArgumentException $e) {
            if (class_exists($driver)) {
                return $this->app->make($driver);
            }

            throw $e;
        }
    }

    /**
     * Get the default channel driver names.
     *
     * @return array
     */
    public function getDefaultDriver()
    {
        return $this->defaultChannels;
    }

    /**
     * Get the default channel driver names.
     *
     * @return array
     */
    public function deliversVia()
    {
        return $this->getDefaultDriver();
    }

    /**
     * Set the default channel driver names.
     *
     * @param  array|string  $channels
     * @return void
     */
    public function deliverVia($channels)
    {
        $this->defaultChannels = (array) $channels;
    }
}