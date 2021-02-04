package org.example.NewsCollector.model;

import org.example.NewsCollector.pojo.ContentPojo;

import java.util.List;

public class Data {

    private String category;

    private List<ContentPojo> listContent;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setListContent(List<ContentPojo> listContent) {
        this.listContent = listContent;
    }

    public String getCategory() {
        return category;
    }

    public List<ContentPojo> getListContent() {
        return listContent;
    }

}