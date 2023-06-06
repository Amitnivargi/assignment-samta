package com.samtaInfotech.assignment.Repositories;

import com.samtaInfotech.assignment.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query(value = "SELECT * FROM questions ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("count") int count);

    @Query(value = "SELECT * FROM questions WHERE id > :questionId ORDER BY id LIMIT :count", nativeQuery = true)
    List<Question> findNextQuestions(@Param("questionId") long questionId, @Param("count") int count);
}
