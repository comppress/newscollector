package org.example.NewsCollector;

import org.example.NewsCollector.controller.ContentController;
import org.example.NewsCollector.parser.Parser;
import org.example.NewsCollector.pojo.ContentPojo;
import org.example.NewsCollector.repository.ContentRepository;
import org.example.NewsCollector.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

@SpringBootApplication
public class NewsCollectorApplication extends SpringBootServletInitializer {

    Logger logger = LoggerFactory.getLogger(ContentController.class);

    public static void main(String[] args) {
        SpringApplication.run(NewsCollectorApplication.class, args);
    }

    @Value( "${personal.settings.time:UTC}" )
    private String timeZone;

    @PostConstruct
    public void init(){
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        logger.info("Time: " + new Date());

    }
}
