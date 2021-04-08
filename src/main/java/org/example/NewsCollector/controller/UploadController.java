package org.example.NewsCollector.controller;

import org.example.NewsCollector.model.Content;
import org.example.NewsCollector.model.Publisher;
import org.example.NewsCollector.model.Rating;
import org.example.NewsCollector.model.RssFeed;
import org.example.NewsCollector.pojo.FeedPojo;
import org.example.NewsCollector.repository.ContentRepository;
import org.example.NewsCollector.repository.PublisherRepository;
import org.example.NewsCollector.repository.RatingRepository;
import org.example.NewsCollector.repository.RssFeedRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class UploadController {

    Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    RssFeedRepository rssFeedRepository;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    PublisherRepository publisherRepository;

    @RequestMapping("/uploadCSV")
    public ModelAndView uploadCSVPage(){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("uploadCSV");
        return modelAndView;
    }

    @RequestMapping("/uploadPublisherInformation")
    public ModelAndView uploadPublisherInformation(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("uploadPublisherInformation");
        return modelAndView;
    }

    @PostMapping("receivePublisherInformation")
    public  @ResponseBody String receivePublisherInformation(@RequestParam("fileToUpload") final MultipartFile uploadingFile) throws IOException {

        if(!uploadingFile.getOriginalFilename().endsWith(".csv")) {
            logger.debug("Wrong file format, *.csv is expected");
            return "wrong file format, should be *.csv";
        }

        if(uploadingFile == null || uploadingFile.isEmpty()) return "File is null or empty";

        final Path tempDirWithPrefix = Files.createTempDirectory("json");
        final File file = new File(tempDirWithPrefix.toFile(), uploadingFile.getOriginalFilename());
        uploadingFile.transferTo(file);

        //News Agency, Description ... Update or enter, then log what has been changed
        
        return "Lala";
    }
    // utf 8 erste, zeile löschen, csv only format
    @PostMapping("receiveCSV")
    public  @ResponseBody String receiveXml(@RequestParam("fileToUpload") final MultipartFile uploadingFile) throws IOException {

        if(!uploadingFile.getOriginalFilename().endsWith(".csv")) {
            logger.debug("Wrong file format, *.csv is expected");
            return "wrong file format, should be *.csv";
        }

        final Path tempDirWithPrefix = Files.createTempDirectory("json");
        final File file = new File(tempDirWithPrefix.toFile(), uploadingFile.getOriginalFilename());
        uploadingFile.transferTo(file);

        String line = "";
        String cvsSplitBy = ";";
        String cvsSplitBy2 = "\";\"";
        List<FeedPojo> feedPojoList = new ArrayList<FeedPojo>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String [] feeds = line.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if(feeds.length <= 1){
                    String test = "";
                }
                for(int i = 0; i < feeds.length; i++){
                    //Cut of first and last char
                    feeds[i] = feeds[i].substring(1);
                    feeds[i] = feeds[i].substring(0, feeds[i].length() - 1);
                }

                logger.debug("publisher " + feeds[0] + " category " + feeds[1] + " rssfeed " + feeds[2]);
                FeedPojo feedPojo = new FeedPojo();
                feedPojo.setPublisher(feeds[0]);
                feedPojo.setCategory(feeds[1]);
                feedPojo.setRssFeedLink(feeds[2]);
                feedPojoList.add(feedPojo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<RssFeed> rssFeedsDB =  rssFeedRepository.findAll();
        logger.info(rssFeedsDB.size() + " Feeds in DB, " + feedPojoList.size() + " Feeds in CSV");

        //Delete Part
        int countDeleteFeeds = 0;
        for(RssFeed rssFeed:rssFeedsDB){

            boolean deleteFeed = true;

            for(FeedPojo feedPojo:feedPojoList){
                /*
                System.out.println(feedPojo.getCategory());
                System.out.println(rssFeed.getCategory());
                System.out.println(feedPojo.getRssFeedLink());
                System.out.println(rssFeed.getLinkRssFeed());
                */
                if(feedPojo.getCategory().equals(rssFeed.getCategory()) && feedPojo.getRssFeedLink().equals(rssFeed.getLinkRssFeed())){
                    logger.debug("feed match");
                    logger.debug(feedPojo.getCategory() + " == " + rssFeed.getCategory() + " && " + feedPojo.getRssFeedLink() + " == " + rssFeed.getLinkRssFeed());
                    deleteFeed = false;
                    break;
                }
            }

            // delete feed, dependent Content and Ratings in DB
            if(deleteFeed){

                countDeleteFeeds++;

                List<Content> contentList =  contentRepository.findByRssFeedId(rssFeed.getId());
                // Delete all Ratings for Content, foreign key constraint
                for(Content content:contentList){
                    List<Rating> ratingList = ratingRepository.findByContentId(content.getId());
                    ratingRepository.deleteAll(ratingList);
                }

                contentRepository.deleteAll(contentList);
                contentRepository.flush();

                rssFeedRepository.delete(rssFeed);

                // Delete ALl Content from Feed
                    // Delete Ratings? Or ignore
            }
        }
        rssFeedRepository.flush();
        logger.info(countDeleteFeeds + " Feeds deleted");
        //Add Part

        int countAddFeeds = 0;

        for(FeedPojo feedPojo:feedPojoList){
            if(!rssFeedRepository.existsByLinkRssFeed(feedPojo.getRssFeedLink())){

                countAddFeeds++;

                Long publisherId = findOrSetPublisherId(feedPojo);

                RssFeed rssFeed = new RssFeed();
                rssFeed.setLinkRssFeed(feedPojo.getRssFeedLink());
                rssFeed.setCategory(feedPojo.getCategory());
                rssFeed.setPublisherId(publisherId);
                rssFeedRepository.save(rssFeed);
            };
        }
        logger.info(countAddFeeds + " added Feeds");

        rssFeedRepository.flush();
        logger.debug("Successfully finished csv file import");
        return "Success <br> Wrote " + countAddFeeds + " new RSS Feeds to DB <br> Deleted " + countDeleteFeeds + " RSS Feeds in DB";
    }


    /**
     * Publisher (id | news_agency | link_homepage)
     * @param feedPojo
     * @return
     */
    public Long findOrSetPublisherId(FeedPojo feedPojo) {

        String rssFeedLink = feedPojo.getRssFeedLink();
        //check if publisher exists by parsing string (domain)

        String domain = getBaseDomain(rssFeedLink);

        Long id = -1L;

        if(publisherRepository.existsByLinkHomepage(domain)){
            id = publisherRepository.findByLinkHomepage(domain).getId();
        }else{
            Publisher publisher = new Publisher();
            publisher.setLinkHomepage(domain);
            publisher.setNewsAgency(feedPojo.getPublisher());
            Publisher newPublisher = publisherRepository.saveAndFlush(publisher);
            id = newPublisher.getId();
        }
        //if not create new publisher

        return id;
    }

    // https://stackoverflow.com/questions/4826061/what-is-the-fastest-way-to-get-the-domain-host-name-from-a-url
    public static String getHost(String url){
        if(url == null || url.length() == 0)
            return "";

        int doubleslash = url.indexOf("//");
        if(doubleslash == -1)
            doubleslash = 0;
        else
            doubleslash += 2;

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        int port = url.indexOf(':', doubleslash);
        end = (port > 0 && port < end) ? port : end;

        return url.substring(doubleslash, end);
    }

    // https://stackoverflow.com/questions/4826061/what-is-the-fastest-way-to-get-the-domain-host-name-from-a-url
    public static String getBaseDomain(String url) {
        String host = getHost(url);

        int startIndex = 0;
        int nextIndex = host.indexOf('.');
        int lastIndex = host.lastIndexOf('.');
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf('.', startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }

}
