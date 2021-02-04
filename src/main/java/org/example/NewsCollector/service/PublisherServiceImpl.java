package org.example.NewsCollector.service;

import org.example.NewsCollector.model.Publisher;
import org.example.NewsCollector.model.RssFeed;
import org.example.NewsCollector.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class PublisherServiceImpl implements PublisherService{

    @Autowired
    PublisherRepository publisherRepository;

    @Override
    public void deleteAllEntries() {
        publisherRepository.deleteAll();
    }

    @Override
    public Publisher getPublisherId(String newsAgency) {
        Publisher publisher = publisherRepository.findByNewsAgency(newsAgency);
        return publisher;
    }

}
