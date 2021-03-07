package org.example.NewsCollector.service;

import org.example.NewsCollector.model.RssFeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RssFeedService {

    public void deleteAllEntries();

    public Long getId(String url);

    public ArrayList<URL> getAllUrls();

    public HashMap<String,String> getAllUrlsWithCategory();

    public Integer getNumberRssFeeds();

    public void writeToDB(List<RssFeed> rssFeedList);

    public boolean rssFeedExists(String linkRssFeed);

    public List<String> getAllCategories();

    public Long getPublisherId(String linkRssFeed);

    RssFeed findByLinkRssFeed(String url);

}
