package org.example.NewsCollector.pojo;

public class FeedPojo {

    public String publisher;
    public String category;
    public String rssFeedLink;

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRssFeedLink(String rssFeedLink) {
        this.rssFeedLink = rssFeedLink;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCategory() {
        return category;
    }

    public String getRssFeedLink() {
        return rssFeedLink;
    }
}
