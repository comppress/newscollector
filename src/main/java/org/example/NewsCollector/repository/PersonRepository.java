package org.example.NewsCollector.repository;

import org.example.NewsCollector.model.Person;
import org.example.NewsCollector.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person,Long> {

}
