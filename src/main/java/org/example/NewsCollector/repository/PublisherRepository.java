package org.example.NewsCollector.repository;

import org.example.NewsCollector.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher,Long> {

    public Publisher findByNewsAgency(String newsAgency);

    public boolean existsByLinkHomepage(String linkHomepage);

    public Publisher findByLinkHomepage(String linkHomepage);

}
