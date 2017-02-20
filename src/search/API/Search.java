package search.API;

import search.SearchListener;
import tweet.Tweet;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class Search {

    private static final int SESSION_TWEETS_NUMBER = 10;

    private static Twitter sTwitter;

    private SearchListener mListener;
    private String mQuery;

    static {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("1DbQ9cgiYi83QvRlZYN6G0EhB")
                .setOAuthConsumerSecret("FFvrv9QZdOIGLNnzxpxEFtSkBgrDWph6TvFpC1QwMoYoY2Ztx1")
                .setOAuthAccessToken("385498014-3ODBkhcxlIO7qthICAJLJ2MzJkdGqR0pMkMgChqC")
                .setOAuthAccessTokenSecret("j6WMid9xGO8QVBSkwUlvRDbpYieR6BBqkBm27YCZmEIAq");
        TwitterFactory tf = new TwitterFactory(cb.build());
        sTwitter = tf.getInstance();
    }

    public Search(String query, SearchListener listener) {
        mListener = listener;
        mQuery = query;
    }

    public void start() {
        for (UnitedStatesZones zone :
                UnitedStatesZones.values()) {
            new Thread(() -> {

                Query twitterQuery = new Query(mQuery);
                twitterQuery.setGeoCode(zone.geoLocation(), zone.radius(), Query.Unit.km);
                twitterQuery.setCount(SESSION_TWEETS_NUMBER);

                long lastID = Long.MAX_VALUE;
                while (true) {
                    try {
                        List<Status> newStatuses = sTwitter.search(twitterQuery).getTweets();

                        for (Status t : newStatuses) {
                            if (t.getId() < lastID) {
                                lastID = t.getId();
                            }
                            if (t.getGeoLocation() != null) {
                                mListener.onTweetReady(new Tweet(
                                        t.getText(),
                                        t.getGeoLocation().getLatitude(),
                                        t.getGeoLocation().getLongitude(),
                                        t.getFavoriteCount(),
                                        t.getRetweetCount()
                                ));
                            }
                        }

                        twitterQuery.setMaxId(lastID - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }).start();
        }
    }
}