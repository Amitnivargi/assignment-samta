package com.samtaInfotech.assignment.Controller;

import com.samtaInfotech.assignment.Entity.Question;
import com.samtaInfotech.assignment.Repositories.QuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class QuestionController {
    private final QuestionRepository questionRepository;
    private final RestTemplate restTemplate;

    public QuestionController(QuestionRepository questionRepository, RestTemplate restTemplate) {
        this.questionRepository = questionRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/play")
    public ResponseEntity<Map<String, Object>> getQuestion() {
        List<Question> questions = questionRepository.findRandomQuestions(5);
        if (questions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Question question = questions.get(0);
        Map<String, Object> response = new HashMap<>();
        response.put("question_id", question.getId());
        response.put("question", question.getQuestion());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/next")
    public ResponseEntity<Map<String, Object>> answerQuestion(@RequestBody Map<String, Object> payload) {
        Long questionId = Long.parseLong(payload.get("question_id").toString());
        String answer = payload.get("answer").toString();

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Question question = optionalQuestion.get();
        Map<String, Object> response = new HashMap<>();
        response.put("correct_answer", question.getAnswer());

        List<Question> nextQuestions = questionRepository.findNextQuestions(question.getId(), 1);
        if (!nextQuestions.isEmpty()) {
            Question nextQuestion = nextQuestions.get(0);
            Map<String, Object> nextQuestionMap = new HashMap<>();
            nextQuestionMap.put("question_id", nextQuestion.getId());
            nextQuestionMap.put("question", nextQuestion.getQuestion());
            response.put("next_question", nextQuestionMap);
        }

        return ResponseEntity.ok(response);
    }

    @Scheduled(fixedRate = 600000) // Runs every 10 minutes
    public void fetchAndStoreQuestions() {
        String apiUrl = "https://jservice.io/api/random?count=5";
        ResponseEntity<Question[]> response = restTemplate.getForEntity(apiUrl, Question[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Question[] questions = response.getBody();
            if (questions != null) {
                questionRepository.saveAll(Arrays.asList(questions));
            }
        }
    }
}