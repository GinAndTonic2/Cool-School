package com.coolSchool.CoolSchool.serviceTest;

import com.coolSchool.CoolSchool.exceptions.userQuiz.UserQuizNotFoundException;
import com.coolSchool.CoolSchool.exceptions.userQuiz.ValidationUserQuizException;
import com.coolSchool.CoolSchool.models.dto.common.UserQuizDTO;
import com.coolSchool.CoolSchool.models.entity.Quiz;
import com.coolSchool.CoolSchool.models.entity.User;
import com.coolSchool.CoolSchool.models.entity.UserAnswer;
import com.coolSchool.CoolSchool.models.entity.UserQuiz;
import com.coolSchool.CoolSchool.repositories.QuizRepository;
import com.coolSchool.CoolSchool.repositories.UserAnswerRepository;
import com.coolSchool.CoolSchool.repositories.UserQuizRepository;
import com.coolSchool.CoolSchool.repositories.UserRepository;
import com.coolSchool.CoolSchool.services.impl.UserQuizServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQuizServiceImplTest {
    @Mock
    private UserQuizRepository userQuizRepository;

    @InjectMocks
    private UserQuizServiceImpl userQuizService;

    private ModelMapper modelMapper;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private UserRepository userRepository;
    private Validator validator;
    @Mock
    private UserAnswerRepository userAnswerRepository;


    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        userQuizService = new UserQuizServiceImpl(userQuizRepository, modelMapper, validator, userAnswerRepository, userRepository, quizRepository);
    }

    @Test
    public void testDeleteUserQuiz_UserQuizPresent() {
        Long userQuizId = 1L;

        UserQuiz userQuiz = new UserQuiz();
        userQuiz.setDeleted(false);

        Optional<UserQuiz> userQuizOptional = Optional.of(userQuiz);

        when(userQuizRepository.findByIdAndDeletedFalse(userQuizId)).thenReturn(userQuizOptional);
        when(userQuizRepository.save(any(UserQuiz.class))).thenReturn(userQuiz);

        assertDoesNotThrow(() -> userQuizService.deleteUserQuiz(userQuizId));
        assertTrue(userQuiz.isDeleted());
        verify(userQuizRepository, times(1)).save(userQuiz);
    }

    @Test
    void testGetAllUserQuizzes() {
        List<UserQuiz> userQuizList = new ArrayList<>();
        userQuizList.add(new UserQuiz());
        Mockito.when(userQuizRepository.findByDeletedFalse()).thenReturn(userQuizList);
        List<UserQuizDTO> result = userQuizService.getAllUserQuizzes();
        assertNotNull(result);
        assertEquals(userQuizList.size(), result.size());
    }

    @Test
    void testGetUserQuizById() {
        Long userQuizId = 1L;
        UserQuiz userQuiz = new UserQuiz();
        Optional<UserQuiz> userQuizOptional = Optional.of(userQuiz);
        when(userQuizRepository.findByIdAndDeletedFalse(userQuizId)).thenReturn(userQuizOptional);
        UserQuizDTO result = userQuizService.getUserQuizById(userQuizId);
        assertNotNull(result);
    }

    @Test
    void testGetUserQuizByIdNotFound() {
        Long userQuizId = 1L;
        Optional<UserQuiz> userQuizOptional = Optional.empty();
        when(userQuizRepository.findByIdAndDeletedFalse(userQuizId)).thenReturn(userQuizOptional);
        assertThrows(UserQuizNotFoundException.class, () -> userQuizService.getUserQuizById(userQuizId));
    }

    @Test
    void testCreateUserQuiz() {
        UserQuizDTO userQuizDTO = new UserQuizDTO();
        UserQuiz userQuiz = modelMapper.map(userQuizDTO, UserQuiz.class);
        when(userQuizRepository.save(any(UserQuiz.class))).thenReturn(userQuiz);
        when(quizRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new Quiz()));
        when(userRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new User()));
        UserQuizDTO result = userQuizService.createUserQuiz(userQuizDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdateUserQuiz() {
        Long userQuizId = 1L;
        UserQuizDTO updatedUserQuizDTO = new UserQuizDTO();
        UserQuiz existingUserQuiz = new UserQuiz();
        Optional<UserQuiz> existingUserQuizOptional = Optional.of(existingUserQuiz);
        when(userQuizRepository.findByIdAndDeletedFalse(userQuizId)).thenReturn(existingUserQuizOptional);
        when(userQuizRepository.save(any(UserQuiz.class))).thenReturn(existingUserQuiz);
        when(quizRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new Quiz()));
        when(userRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new User()));
        UserQuizDTO result = userQuizService.updateUserQuiz(userQuizId, updatedUserQuizDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdateUserQuizNotFound() {
        Long nonExistentUserQuizId = 99L;
        UserQuizDTO updatedUserQuizDTO = new UserQuizDTO();
        when(userQuizRepository.findByIdAndDeletedFalse(nonExistentUserQuizId)).thenReturn(Optional.empty());
        assertThrows(UserQuizNotFoundException.class, () -> userQuizService.updateUserQuiz(nonExistentUserQuizId, updatedUserQuizDTO));
    }

    @Test
    void testDeleteUserQuizNotFound() {
        Long nonExistentUserQuizId = 99L;

        when(userQuizRepository.findByIdAndDeletedFalse(nonExistentUserQuizId)).thenReturn(Optional.empty());

        assertThrows(UserQuizNotFoundException.class, () -> userQuizService.deleteUserQuiz(nonExistentUserQuizId));
    }

    @Test
    void testCreateUserQuiz_ValidationException() {
        UserQuizDTO userQuizDTO = new UserQuizDTO();
        userQuizDTO.setFeedback(null);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);

        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation error", violations);
        when(quizRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new Quiz()));
        when(userRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new User()));
        when(userQuizRepository.save(any(UserQuiz.class))).thenThrow(constraintViolationException);

        assertThrows(ValidationUserQuizException.class, () -> userQuizService.createUserQuiz(userQuizDTO));
    }

    @Test
    void testUpdateUserQuiz_ValidationException() {
        Long userQuizId = 1L;
        UserQuizDTO userQuizDTO = new UserQuizDTO();
        userQuizDTO.setFeedback(null);
        UserQuiz existingUserQuiz = new UserQuiz();
        Optional<UserQuiz> existingUserQuizOptional = Optional.of(existingUserQuiz);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);

        ConstraintViolationException constraintViolationException = new ConstraintViolationException("Validation error", violations);

        when(userQuizRepository.findByIdAndDeletedFalse(userQuizId)).thenReturn(existingUserQuizOptional);
        when(userQuizRepository.save(any(UserQuiz.class))).thenThrow(constraintViolationException);
        when(quizRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new Quiz()));
        when(userRepository.findByIdAndDeletedFalse(any())).thenReturn(Optional.of(new User()));
        assertThrows(ConstraintViolationException.class, () -> userQuizService.updateUserQuiz(userQuizId, userQuizDTO));
    }

    @Test
    void calculateTotalMarksForQuizAttempt_ShouldCalculateTotalMarks() {
        UserQuiz userQuiz = new UserQuiz();
        List<UserAnswer> userAnswers = new ArrayList<>();
        UserAnswer userAnswer = new UserAnswer();
        userAnswers.add(userAnswer);
        when(userAnswerRepository.findByUserAndAttemptNumber(userQuiz.getUser(), userQuiz.getAttemptNumber())).thenReturn(userAnswers);
        BigDecimal expectedTotalMarks = BigDecimal.valueOf(0);
        BigDecimal totalMarks = userQuizService.calculateTotalMarksForQuizAttempt(userQuiz);
        assertEquals(expectedTotalMarks, totalMarks);
    }

    @Test
    void getUserAnswersForQuizAttempt_ShouldReturnUserAnswers() {
        UserQuiz userQuiz = new UserQuiz();
        List<UserAnswer> expectedUserAnswers = new ArrayList<>();
        UserAnswer userAnswer = new UserAnswer();
        expectedUserAnswers.add(userAnswer);
        when(userAnswerRepository.findByUserAndAttemptNumber(userQuiz.getUser(), userQuiz.getAttemptNumber())).thenReturn(expectedUserAnswers);
        List<UserAnswer> userAnswers = userQuizService.getUserAnswersForQuizAttempt(userQuiz);
        assertEquals(expectedUserAnswers, userAnswers);
    }
}

