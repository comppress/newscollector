package org.example.NewsCollector.controller;

import com.sun.net.httpserver.Authenticator;
import org.example.NewsCollector.Information.Information;
import org.example.NewsCollector.model.Content;
import org.example.NewsCollector.model.Publisher;
import org.example.NewsCollector.model.RssFeed;
import org.example.NewsCollector.parser.Parser;
import org.example.NewsCollector.service.ContentService;
import org.example.NewsCollector.service.PublisherService;
import org.example.NewsCollector.service.RssFeedService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

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

    //difference get mapping?
    @RequestMapping("/info")
    public @ResponseBody String info(){

        String responseBody = "Newscontroller running since: " + Information.START_TIME_NEWSCONTROLLER + " <br>" +
                "News wrote to DataBase: " + Information.CONTENT_SUBMITTED_TO_DB.toString() + " <br>" +
                "Total News in the DataBase: " + contentService.getNumberContent().toString() + "<br>" +
                "Fetching Data from different Rss Feeds: " + rssFeedService.getNumberRssFeeds() + " <br>";

        return responseBody;
    }

    @RequestMapping("/uploadJson")
    public ModelAndView uploadJsonPage(){

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("upload");
            return modelAndView;
    }

    @PostMapping("receiveJson")
    public  @ResponseBody String receiveXml(@RequestParam("fileToUpload") final MultipartFile uploadingFile) throws IOException {

        if(!uploadingFile.getOriginalFilename().endsWith(".json")) {
            logger.debug("Wrong file format, *.json is expected");
            return "wrong file format, should be *.json";
        }

        final Path tempDirWithPrefix = Files.createTempDirectory("json");
        final File file = new File(tempDirWithPrefix.toFile(), uploadingFile.getOriginalFilename());
        uploadingFile.transferTo(file);

        JSONParser parser = new JSONParser();
        ArrayList<RssFeed> rssFeedArrayList = new ArrayList<>();
            try {
                System.out.println(file);
                System.out.println(file.getAbsolutePath());
                Object obj = parser.parse(new FileReader(file));

                // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
                JSONObject jsonObject = (JSONObject) obj;

                // A JSON array. JSONObject supports java.util.List interface.
                JSONArray publisherList = (JSONArray) jsonObject.get("PublisherList");

                // An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework.
                // Iterators differ from enumerations in two ways:
                // 1. Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
                // 2. Method names have been improved.
                Iterator<JSONObject> iterator = publisherList.iterator();
                while (iterator.hasNext()) {
                    JSONObject item = iterator.next();
                    String publisher = item.get("Publisher").toString();
                    String category = item.get("Category").toString();
                    String rssFeedLink = item.get("RSS Feed").toString();

                    // check if already exists in db
                    if(rssFeedService.rssFeedExists(rssFeedLink))continue;
                    //add
                    RssFeed rssFeedObject = createRssFeed(publisher,category,rssFeedLink);
                    rssFeedArrayList.add(rssFeedObject);
                }
                logger.debug("Write to DB");
            // write to db
            rssFeedService.writeToDB(rssFeedArrayList);
            logger.debug("Wrote " + rssFeedArrayList.size() + " new RSS Feeds to DB");
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            return "Error check the Stack trace \n " + errorMessage;
        }
        logger.debug("Successfully finished json file import");
        return "Success <br> Wrote " + rssFeedArrayList.size() + " new RSS Feeds to DB <br>";
    }

    private RssFeed createRssFeed(String publisher, String category, String rssFeedLink ){
        RssFeed rssFeed = new RssFeed();
        rssFeed.setCategory(category);
        rssFeed.setLinkRssFeed(rssFeedLink);
        Publisher publisher1  = publisherService.getPublisherId(publisher);
        if(publisher1 == null){

            logger.error("Publisher is not in db, please import the publisher first");
            logger.error("Missing Publisher" + publisher);
            throw new ExceptionInInitializerError();
        }
        rssFeed.setPublisherId(publisher1.getId());
        System.out.println("Test");
        return rssFeed;
    }

    @RequestMapping("/truncateTables")
    public @ResponseBody String truncateTables() {

        contentService.deleteAllEntries();
        rssFeedService.deleteAllEntries();
        publisherService.deleteAllEntries();

        /*
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("messageString","truncate tables" );
        modelAndView.setViewName("upload");
        return modelAndView;
        */

        return "Success, deleted Publisher, Content and RssFeed Table";
    }

}
