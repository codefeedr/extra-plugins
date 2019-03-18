# extra-plugins
[![Build Status](https://travis-ci.org/codefeedr/extra-plugins.svg?branch=master)](https://travis-ci.org/codefeedr/extra-plugins)
A set of not-so-important CodeFeedr plugins. 

## Plugins
- [RSS](#rss)
- [Travis](#travis)
- [Weblogs](#weblogs)
- [Twitter](#twitter)


# RSS
A plugin with a stage for reading from a basic RSS feed using polling.

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-rss" % "0.1.0"
```

### Configuration

An RSS input stage requires the address of the feed, the date format used in the `pubDate`, and the number of
milliseconds between each poll.

```scala
val dateFormat = "EEE, dd MMMM yyyy HH:mm:ss z"
val pollingInterval = 1000 // once per second
val rss = new RSSInput("http://example.com/feed.xml", dateFormat, pollingInterval)
```

The result is a stream of `RSSItem` objects with title, date, link, category and guid.
# Travis
The Travis plugin provides two transform stages to interact with the Travis API. It relies on the Github plugin because
because it uses the output of the Github push event stage (see [Github](github)).

## Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-travis" % "0.1.0"
```

## Stages

The plugin contains two stages: 

- The `TravisFilterActiveReposTransformStage` takes a push-event stream from the GitHubEventToPushEvent Stage (see
[Github](github)) and filters it to only keep push events from repos that are active on Travis
 
- The `TravisPushEventBuildInfoTransformStage` takes a push-event from active Travis repositories stream and requests
the build information from Travis. The constructor only has one parameter:
  
    - `capacity`: to specify the amount of builds that are simultaneously requested per Flink thread (`100` is the 
    default).


### Configuration


**NOTE**: This plugin needs API access to TravisCI and requires keys. It will request keys from a key manager
with the target `travis`.

### Examples
```scala
new PipelineBuilder()
  .setBufferType(BufferType.Kafka)
  .append(new GitHubEventsInput())
  .append(new GitHubEventToPushEvent)
  .append(new TravisFilterActiveReposTransformStage())
  .append(new TravisPushEventBuildInfoTransformStage(capacity = 10))
  .build()
  .startLocal()
```
This pipeline will create a real-time Travis build stream by:

- Reading from a the `/events` endpoint (GitHubEventsInput stage)
- Filter (& parse) the PushEvents (GitHubEventsToPushEvent stage)
- Filter the push events from repositories that are active on Travis
- Request build information of those push events

**Note**: These samples do not show configuration of for instance key management. See the sections above to show the
configurable options.

# Weblogs
Plugin that provides input stages for web server logs.

Currently supports default Apache httpd access logs. NginX support is welcome through a pull request.

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-weblogs" % "0.1.0"
```

### Configuration

The Apache Httpd log input creates a stream of `HttpdLogItem` events from a file. The `HttpdLogItem` case class
contains every part of a log line parsed into Scala format.

```scala
val log = HttpdLogInput("/var/httpd/access.log")
```

### Notes

This plugin currently does only read the file once and does not monitor for new changes. It is a simple proof of
concept of an input stage.
# Twitter
This plugin provides stages related to the Twitter API. 
It makes uses of the [twitter4s](https://github.com/DanielaSfregola/twitter4s) library, for detailed documentation see their GitHub page.

## Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-twitter" % "0.1.0"
```


## Stages
Currently two stages are provided related to Twitter statuses. The `TwitterStatusInput` is an InputStage which
streams Twitter statuses based on some filters.  The Twitter plugin also provides the `TwitterTrendingStatusInput` to get the tweets regarding the current list of trending topics.

### Configuration
For all the Twitter related stages you must provide the following case classes:

```scala 
case class ConsumerToken(key: String, secret: String)
case class AccessToken(key: String, secret: String)
```

Those tokens can be generated using a [Twitter app](https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html).

#### TwitterStatusInput
The filters of this stage are explained [here](https://github.com/joskuijpers/bep_codefeedr/blob/fd14096544fe5a2390a356bd5cb8781a52e28db8/codefeedr-plugins/codefeedr-twitter/src/main/scala/org/codefeedr/plugin/twitter/stages/TwitterStatusInput.scala#L37) and should be passed through the constructor.

**Note**: At least 1 filter should be given, see [here](https://developer.twitter.com/en/docs/tweets/filter-realtime/api-reference/post-statuses-filter.html).

#### TwitterTrendingStatusInput
For the TwitterTrendingStatusInput a `sleepTime` should be given, this is the amount of minutes to wait before dynamically refreshing the trending topics
and restarting the stream. By default this is 15 minutes. 

**Note:** Retrieving the trending topics is a request that is cached for 5 minutes at the Twitter API. Therefore, setting the `sleepTime` lower than 5 minutes doesn't make any sense. 

## Notes
Twitter also ratelimits its streams, [twitter4s](https://github.com/DanielaSfregola/twitter4s) takes care of that. To get more information
on this ratelimit see [here](https://developer.twitter.com/en/docs/basics/rate-limiting.html).