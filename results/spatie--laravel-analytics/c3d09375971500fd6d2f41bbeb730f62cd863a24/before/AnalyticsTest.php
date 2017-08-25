<?php

namespace Spatie\Analytics\Tests;

use Carbon\Carbon;
use Illuminate\Support\Collection;
use Mockery;
use PHPUnit_Framework_TestCase;
use Spatie\Analytics\Analytics;
use Spatie\Analytics\AnalyticsClient;

class AnalyticsTest extends PHPUnit_Framework_TestCase
{
    /** @var \Spatie\Analytics\AnalyticsClient|\Mockery\Mock */
    protected $analyticsClient;

    /** @var string */
    protected $viewId;

    /** @var Analytics */
    protected $analytics;

    public function setUp()
    {
        Carbon::setTestNow(Carbon::create(2016, 1, 1));

        $this->analyticsClient = Mockery::mock(AnalyticsClient::class);

        $this->viewId = '1234567';

        $this->analytics = new Analytics($this->analyticsClient, $this->viewId);
    }

    public function tearDown()
    {
        Mockery::close();
    }

    /** @test */
    public function it_can_retrieve_the_visitor_and_page_views()
    {
        $expectedArguments = [
            $this->viewId,
            $this->expectCarbonDate('2015-01-01'),
            $this->expectCarbonDate('2016-01-01'),
            'ga:users,ga:pageviews',
            ['dimensions' => 'ga:date'],
        ];

        $this->analyticsClient
            ->shouldReceive('performQuery')->withArgs($expectedArguments)
            ->once()
            ->andReturnNull()
            ->andReturn([
                'rows' => [['20160101', '1', '2']]
            ]);

        $response = $this->analytics->getVisitorsAndPageViews();

        $this->assertInstanceOf(Collection::class, $response);
        $this->assertEquals('2016-01-01', $response->first()['date']->format('Y-m-d'));
        $this->assertEquals(1, $response->first()['visitors']);
        $this->assertEquals(2, $response->first()['pageViews']);
    }

    protected function expectCarbonDate(string $dateString)
    {
        return Mockery::on(function (Carbon $argument) use ($dateString) {
            return $argument->format('Y-m-d H:i:s') == "{$dateString} 00:00:00";
        });
    }
}