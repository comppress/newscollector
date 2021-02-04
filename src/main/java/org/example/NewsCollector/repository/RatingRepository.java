package org.example.NewsCollector.repository;

import org.example.NewsCollector.model.Content;
import org.example.NewsCollector.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating,Long> {

    public boolean existsByContentId(Long contentId);

    public boolean existsByPersonIdAndContentId(Long personId, Long contentId);

    Rating findByPersonIdAndContentId(Long personId, Long contentId);

    List<Rating> findByContentId(Long contentId);

}
