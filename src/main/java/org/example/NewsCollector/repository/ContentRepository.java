package org.example.NewsCollector.repository;

import org.example.NewsCollector.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content,Long> {

    public boolean existsByLink(String link);

    public boolean existsByTitleAndAndImageLink(String title, String imageLink);

    public List<Content> findTop25ByCategory(String category);

    public List<Content> findByRssFeedId(Long rssFeedId);

    @Query(value = "SELECT * FROM content WHERE category= :category ORDER BY id DESC LIMIT :listLenght", nativeQuery = true)
    public List<Content> selectContentOrderById(@Param("category") String category, @Param("listLenght") Integer listLenght);

    @Query(value = "SELECT * FROM content WHERE category= :category ORDER BY creation_date DESC LIMIT :listLenght", nativeQuery = true)
    public List<Content> selectContentOrderByCreationDate(@Param("category") String category, @Param("listLenght") Integer listLenght);

    @Query(value = "SELECT * FROM content WHERE category= :category ORDER BY id DESC", nativeQuery = true)
    public List<Content> selectContentOrderById(@Param("category") String category);

}
