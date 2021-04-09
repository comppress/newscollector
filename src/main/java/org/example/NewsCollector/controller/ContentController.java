package org.example.NewsCollector.controller;


import org.example.NewsCollector.Information.Information;
import org.example.NewsCollector.model.Content;

import org.example.NewsCollector.parser.Parser;
import org.example.NewsCollector.service.ContentService;
import org.example.NewsCollector.service.PublisherService;
import org.example.NewsCollector.service.RssFeedService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Controller
@EnableScheduling
public class ContentController {

    Logger logger = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    ContentService contentService;

    @Autowired
    Parser parser;

    @Autowired
    RssFeedService rssFeedService;

    @Autowired
    PublisherService publisherService;

    @Scheduled(cron = "* */2 * * * *")
    public void runNewsCollector() {

        logger.info("Started Cron Job");

        HashMap<String, String> mapFeedToChangeDate = new HashMap<String, String>();

        Integer runs = ++Information.COUNT_RUNS_NEWSCONTROLLER;
        logger.debug("Count Runs Parser" + runs.toString());

        logger.info("Starting fetching Content from RSS Feeds");
        ArrayList<Content> listContent = parser.runRssFeedParser(mapFeedToChangeDate);
        logger.info("Fetched " + listContent.size() + " possible new Content");

        if (listContent.size() > 0) {
            logger.debug("Attempting to write to DB");
            contentService.writeToDB(listContent);
            logger.debug("Finished writing to DB");
        } else {
            logger.info(new Date() + " no new Entries");
        }

        logger.info("Finished Cron Job");
    }

    @RequestMapping("/info")
    public @ResponseBody String info(){

        String responseBody = "Newscontroller running since: " + Information.START_TIME_NEWSCONTROLLER + " <br>" +
                "News wrote to DataBase: " + Information.CONTENT_SUBMITTED_TO_DB.toString() + " <br>" +
                "Total News in the DataBase: " + contentService.getNumberContent().toString() + "<br>" +
                "Fetching Data from different Rss Feeds: " + rssFeedService.getNumberRssFeeds() + " <br>";

        return responseBody;
    }

    @RequestMapping("/truncateTables")
    public @ResponseBody String truncateTables() {

        contentService.deleteAllEntries();
        rssFeedService.deleteAllEntries();
        publisherService.deleteAllEntries();

        return "Success, deleted Publisher, Content and RssFeed Table";
    }

}
