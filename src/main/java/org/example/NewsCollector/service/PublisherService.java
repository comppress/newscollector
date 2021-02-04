package org.example.NewsCollector.service;

import org.example.NewsCollector.model.Publisher;
import org.example.NewsCollector.model.RssFeed;

public interface PublisherService {

    public void deleteAllEntries();

    public Publisher getPublisherId(String newsAgency);

}
