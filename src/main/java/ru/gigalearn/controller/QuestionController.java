package ru.gigalearn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.gigalearn.dto.AnswerOptionCreateDto;
import ru.gigalearn.dto.AnswerOptionResponseDto;
import ru.gigalearn.entity.AnswerOption;
import ru.gigalearn.repository.AnswerOptionRepository;
import ru.gigalearn.service.QuizService;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuizService quizService;
    private final AnswerOptionRepository answerOptionRepository;

    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AnswerOptionResponseDto addAnswerOption(@PathVariable Long id, @Valid @RequestBody AnswerOptionCreateDto dto) {
        Long optionId = quizService.addAnswerOption(id, dto.getText(), dto.getIsCorrect());

        AnswerOption option = answerOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Answer option not found"));

        return AnswerOptionResponseDto.builder()
                .id(option.getId())
                .text(option.getText())
                .isCorrect(option.getIsCorrect())
                .questionId(option.getQuestion().getId())
                .questionText(option.getQuestion().getText())
                .build();
    }
}

