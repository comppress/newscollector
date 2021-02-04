package org.example.NewsCollector.service;

import org.example.NewsCollector.Information.Information;
import org.example.NewsCollector.model.Content;
import org.example.NewsCollector.repository.ContentRepository;
import org.example.NewsCollector.repository.RatingRepository;
import org.example.NewsCollector.repository.RssFeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContentServiceImpl implements ContentService{

    Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    ContentRepository contentRepository;

    public void deleteAllEntries(){
        contentRepository.deleteAll();
    }

    /**
     * Take a list of content and Checks if the content either exists by link in the db or if the combination of title and ImageLink exist in the db
     * If not the content is written to the database
     *
     * @param list
     */
    @Override
    public void writeToDB(List<Content> list) {

        int count = 0;

        for(Content content:list) {
            if(contentRepository.existsByLink(content.getLink())){
                continue;
            } if (contentRepository.existsByTitleAndAndImageLink(content.getTitle(),content.getImageLink()) && (content.getImageLink() != "not implemented")){
                logger.info("Skipping Article " + content.getTitle() + "because there is an Entry in the db with the same title and imageLink");
                continue;
            }
            count++;
            Information.CONTENT_SUBMITTED_TO_DB++;
            contentRepository.save(content);
            if(count % 100 == 0) {
                contentRepository.flush();
                logger.debug("Flushed "+count+" changes");
            }
        }
        contentRepository.flush();
        logger.debug("Flushed "+count+" changes");
    }

    @Override
    public boolean contentExists(String url) {
        return contentRepository.existsByLink(url);
    }

    @Override
    public Integer getNumberContent() {
        return contentRepository.findAll().size();
    }

    @Override
    public List<Content> getContentWithCategory(String category) {
            return contentRepository.findTop25ByCategory(category);
    }

    @Override
    public List<Content> getContentWithCategoryAndListLength(String category, Integer listLength) {
        return contentRepository.nativeQueryCategory(category,listLength);
    }

    @Override
    public List<Content> getContentWithCategoryAndListLengthSortedByDate(String category, Integer listLenght) {
        return contentRepository.nativeQueryCategory3(category, listLenght);
    }

    @Override
    public List<Content> getContentWitCategoryAndBestRating(String category, Integer listLength, Integer threshold) {

        return null;
    }

    @Override
    public List<Long> findAllContentFromCategroy(String category) {
        List<Content> contentList = contentRepository.nativeQueryCategory2(category);
        List<Long> idList = new ArrayList<Long>();
        for(Content content: contentList){
            if(ratingRepository.existsByContentId(content.getId())) {
                idList.add(content.getId());
            }
        }
        return idList;
    }
}
