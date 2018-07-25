package com.wordpresss.technicado

import Constants._
import org.apache.spark.SparkContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import twitter4j.Status

class ProcessTweets {

  def setUpTwitter(hdfs_prop_file: String, sparkContext: SparkContext) = {
    ConfigReader.readConfig(hdfs_prop_file, sparkContext)

    System.setProperty("twitter4j.oauth.consumerKey", ConfigReader.getString(TWITTER_CONSUMER_KEY))
    System.setProperty("twitter4j.oauth.consumerSecret", ConfigReader.getString(TWITTER_CONSUMER_SECRET))
    System.setProperty("twitter4j.oauth.accessToken", ConfigReader.getString(TWITTER_ACCESS_TOKEN))
    System.setProperty("twitter4j.oauth.accessTokenSecret", ConfigReader.getString(TWITTER_ACCESS_SECRET))
  }

  def fnExtractHashTags(tweets: ReceiverInputDStream[Status]) = {
    val hashtags = tweets
      .map(tweet => tweet.getText)
      .flatMap(tweet => tweet.split(" "))
      .filter(_.startsWith("#"))
    hashtags
  }

  def readTweetOverWindow(hashtags: DStream[String]) = {
    val hashtagKV = hashtags.map(x => (x, 1))
    val x: DStream[(String, Int)] = hashtagKV.reduceByKeyAndWindow(_ + _, _ - _, Seconds(300), Seconds(2))
    x
  }
}
