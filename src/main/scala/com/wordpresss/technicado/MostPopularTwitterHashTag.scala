package com.wordpresss.technicado

import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.twitter._
import twitter4j.Status

object MostPopularTwitterHashTag {

  Logger.getLogger("org").setLevel(Level.ERROR)
  def main(args: Array[String]): Unit = {
    if(args.length != 1){
      println("USAGE: spark-submit --class com.wordpresss.technicado.MostPopularTwitterHashTag " +
        "--master local[*] spark/jars/mostpopulartwitterhashtag_2.11-0.1.jar hdfs://path/to/twitter.config")
      System.exit(-1)
    }
    val sparkConf = new SparkConf().setAppName("MostPopularTwitterHashTag")
    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val processTweets = new ProcessTweets()
    processTweets.setUpTwitter(args(0), ssc.sparkContext)

    val tweets: ReceiverInputDStream[Status] = TwitterUtils.createStream(ssc, None)
    val hashtags: DStream[String] = processTweets.fnExtractHashTags(tweets)

    // print tweets
    processTweets.readTweetOverWindow(hashtags)
      .transform(rdd => rdd.sortBy(_._2, false)).print

    ssc.checkpoint("/tmp/spark-checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }


}
