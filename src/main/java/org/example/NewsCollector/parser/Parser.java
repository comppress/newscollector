package org.example.NewsCollector.parser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.example.NewsCollector.controller.ContentController;
import org.example.NewsCollector.model.Content;
import org.example.NewsCollector.model.Publisher;
import org.example.NewsCollector.model.RssFeed;
import org.example.NewsCollector.repository.PublisherRepository;
import org.example.NewsCollector.service.RssFeedService;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URLConnection;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class Parser {

    Logger logger = LoggerFactory.getLogger(Parser.class);

    @Autowired
    RssFeedService rssFeedService;

    @Autowired
    PublisherRepository publisherRepository;

    public ArrayList<Content> runRssFeedParser(HashMap<String, String> mapLastChangeDate) {


        ArrayList<URL> listUrl = rssFeedService.getAllUrls();
        logger.debug("Got " + listUrl.size() + " Rss Feeds From Database");
        // get Key value store for url and category
        HashMap<String,String> mapUrlCategory = rssFeedService.getAllUrlsWithCategory();
        ArrayList<Content> listNewsModel = new ArrayList<Content>();

        try {
            listNewsModel = listOfModelNews(mapLastChangeDate, mapUrlCategory);
        } catch (IOException | ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return listNewsModel;

    }

    public ArrayList<Content> listOfModelNews(HashMap<String, String> mapLastBuildDate, HashMap<String, String> mapUrlToCategory)
            throws IOException, ParserConfigurationException {

        ArrayList<Content> listNewsModel = new ArrayList<Content>();

        for (String url: mapUrlToCategory.keySet()) {

            logger.debug("Receiving News from " + url);

            int counterArticelsInFeed = 0;

            String rssFeedString = urlReader(url);
            //String rssFeedString3 = urlReaderOld(url);
            //String rssFeedString2 = urlReaderJsoupHttp(url);

            if(rssFeedString == null) continue;

            String category = mapUrlToCategory.get(url);


            // Convert string to input stream
            InputStream stream = new ByteArrayInputStream(rssFeedString.getBytes("UTF-8"));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            try {
                doc = dBuilder.parse(stream);
            } catch (SAXException e) {
                e.printStackTrace();
                continue;
            }
            doc.getDocumentElement().normalize();

            String lastBuildDate = null;
            try {
                lastBuildDate = parseLastBuildDate(rssFeedString);
            } catch (SAXException e) {
                e.printStackTrace();
                continue;
            }

            if (!lastBuildDate.equals(mapLastBuildDate.get(url.toString()))) {
                mapLastBuildDate.put(url.toString(), lastBuildDate);
            } else {
                System.out.println("Skipping XML(" + url.toString() + ") because Map entry: "
                        + mapLastBuildDate.get(url.toString()) + " equals " + lastBuildDate);
                continue;
            }

            NodeList nList = doc.getElementsByTagName("item");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    counterArticelsInFeed ++;
                    Content content = new Content();
                    content.setAverageRating(0.0);
                    content.setCountRating(0L);
                    content.setSumRating(0.0);
                    content.setCategory(category);
                    RssFeed rssFeed = rssFeedService.findByLinkRssFeed(url.toString());
                    content.setRssFeedId(rssFeed.getId());
                    Optional<Publisher> publisher = publisherRepository.findById(rssFeed.getPublisherId());
                    content.setSource(publisher.get().getNewsAgency());
                    listNewsModel.add(content);

                    Element eElement = (Element) nNode;

                    // is not allowed to be null in database
                    String link = eElement.getElementsByTagName("link").item(0).getTextContent();
                    if (link == null) {
                        link = "url not found error";
                    }

                    content.setLink(link);
                    //TODO
                    /*
                    String pubDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
                    // Example Values Fri, 17 Jul 2020 08:44:43 +0200, Fri, 17 Jul 2020 06:05:00 GMT, Fri, 17 Jul 2020 08:38:00 +0200
                    // Maybe create class for different time patterns from different Newspapers
                    // HH:mm:ss -> and for timezone check GMT and +0200
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    LocalDateTime localDate = LocalDateTime.from(f.parse("January 13, 2012"));

                    if (pubDate == null) {
                        // 'YYYY-MM-DD hh:mm:ss' format
                        String time = LocalDateTime.now().toString();
                        pubDate = "todo";
                    }

                    content.setPubDate(pubDate);
                    */


                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    String unescapedHtmlTitle = StringEscapeUtils.unescapeHtml4(title);
                    content.setTitle(unescapedHtmlTitle);

                    String img = "not implemented";

                    NodeList children = eElement.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        Node p = children.item(i);

                        if (p.getNodeType() == Node.ELEMENT_NODE) {
                            Element elem = (Element) p;

                            if (elem.getTagName() == "enclosure") {
                                img = elem.getAttribute("url");
                            } else if (elem.getTagName() == "media:thumbnail"){
                                img = elem.getAttribute("url");
                            } else if ((elem.getTagName() == "description" || elem.getTagName() == "content:encoded") && elem.getTextContent().contains("src=")){
                                Pattern pattern;
                                if (elem.getTextContent().contains("src=\"")) {
                                    pattern = Pattern.compile("(?<=src=(\"?))(http(s?):/)(.*?)(?=\")");
                                } else {
                                    pattern = Pattern.compile("(?<=src=(\"?))(http(s?):/)(.*?)(?= )");
                                }
                                String textContent = elem.getTextContent();
                                String replaceString = textContent.replace("&amp;","&");
                                Matcher matcher = pattern.matcher(replaceString);
                                if (matcher.find()) {
                                    img = matcher.group();
                                }
                            }
                        }
                    }

                    content.setImageLink(img);
                    // Get the value of the ID attribute.
                    // String ID = node.getAttributes().getNamedItem("ID").getNodeValue();

                }
            }
            logger.debug("Parsed " + counterArticelsInFeed + " articles from " + url);

        }
        return listNewsModel;
    }

    /**
     * Searches for LastBuildDate in XML, if not defined returns hash of XML)
     *
     * @param rssFeedString
     * @return String
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private String parseLastBuildDate(String rssFeedString)
            throws ParserConfigurationException, SAXException, IOException {

        String parseLastBuildDate = "";

        // Convert string to input stream
        InputStream stream = new ByteArrayInputStream(rssFeedString.getBytes("UTF-8"));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(stream);
        doc.getDocumentElement().normalize();

        // Will always return one elem?
        NodeList itemList = doc.getElementsByTagName("lastBuildDate");

        for (int i = 0; i < itemList.getLength(); i++) {
            Node p = itemList.item(i);

            if (p.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) p;

                if (elem.getTagName() == "lastBuildDate") {
                    parseLastBuildDate = elem.getTextContent();
                    break;
                }
            }
        }

        if (parseLastBuildDate != null && !parseLastBuildDate.isEmpty()) {
            return parseLastBuildDate;
        } else {
            return Integer.toString(rssFeedString.hashCode());
        }
    }

    // https://zetcode.com/java/readwebpage/

    //https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
    private String urlReaderHttp(String url){

        String out = new String();

        try {
            out = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    private String urlReaderJsoupHttp(String url) throws IOException {

        HashMap<String,String> header = new HashMap<>();
        String html = Jsoup.connect(url).header("Content-Type","text/plain;charset=UTF-8").get().html();

        return html;
    }


    // http://zetcode.com/java/readwebpage/
    private String urlReader(String sourceUrl) throws IOException {

            HttpGet request = null;
            String content = null;

            try {
                String url = sourceUrl;

                int timeout = 5;
                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(timeout * 1000)
                        .setConnectionRequestTimeout(timeout * 1000)
                        .setSocketTimeout(timeout * 1000).build();
                HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
                request = new HttpGet(url);
                request.addHeader("Content-Type","text/plain;charset=UTF-8");
                request.addHeader("User-Agent", "Apache HTTPClient");
     //           request.addHeader("Accept-Encoding", "UTF-8");
                HttpResponse response = client.execute(request);

                HttpEntity entity = response.getEntity();
                content = EntityUtils.toString(entity, "UTF-8");

            } finally {

                if (request != null) {

                    request.releaseConnection();
                }
            }

            return content;
    }

    @Deprecated
    // source: https://www.techiedelight.com/read-contents-of-url-into-string-java/
    private String urlReaderOld(String sourceUrl) throws IOException {

        if(sourceUrl.startsWith("http:")){
            // http://zetcode.com/java/readwebpage/
            String webPage = sourceUrl;
            String html = Jsoup.connect(webPage).get().html();
            return html;
        }

        URL url = null;

        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String line;

        InputStream in = null;
        try {
            in = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            //String s = reader.readLine();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return sb.toString();
    }


    public ArrayList<URL> parseUrlFromXml(String pathXMLFile) {

        ArrayList<URL> listUrls = new ArrayList<URL>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            URL u = Parser.class.getClassLoader().getResource("mediaUrls.xml");

            String s = u.toString();
            // ugly hack need to change this
            // s = s.replace("file:","");
            // System.out.println(s);
            // "/Users/lucasstocksmeier/Spielwiese/parser/target/mediaUrls.xml"
            Document doc = builder.parse(s);
            NodeList itemList = doc.getElementsByTagName("url");

            for (int i = 0; i < itemList.getLength(); i++) {
                Node p = itemList.item(i);

                if (p.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) p;

                    if (elem.getTagName() == "url") {
                        URL url = new URL(elem.getTextContent());
                        listUrls.add(url);

                    }
                }
            }

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return listUrls;
    }



}
