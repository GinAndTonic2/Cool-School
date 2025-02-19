package com.coolSchool.CoolSchool.services.impl;

import com.coolSchool.CoolSchool.enums.Role;
import com.coolSchool.CoolSchool.exceptions.blog.ValidationBlogException;
import com.coolSchool.CoolSchool.exceptions.common.AccessDeniedException;
import com.coolSchool.CoolSchool.exceptions.course.CourseNotFoundException;
import com.coolSchool.CoolSchool.exceptions.review.ReviewNotFoundException;
import com.coolSchool.CoolSchool.exceptions.user.UserNotFoundException;
import com.coolSchool.CoolSchool.models.dto.auth.PublicUserDTO;
import com.coolSchool.CoolSchool.models.dto.request.ReviewRequestDTO;
import com.coolSchool.CoolSchool.models.dto.response.CourseResponseDTO;
import com.coolSchool.CoolSchool.models.dto.response.ReviewResponseDTO;
import com.coolSchool.CoolSchool.models.entity.Course;
import com.coolSchool.CoolSchool.models.entity.Review;
import com.coolSchool.CoolSchool.repositories.CourseRepository;
import com.coolSchool.CoolSchool.repositories.ReviewRepository;
import com.coolSchool.CoolSchool.repositories.UserRepository;
import com.coolSchool.CoolSchool.services.CourseService;
import com.coolSchool.CoolSchool.services.ReviewService;
import jakarta.validation.ConstraintViolationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, CourseService courseService, UserRepository userRepository, CourseRepository courseRepository, ModelMapper modelMapper) {
        this.reviewRepository = reviewRepository;
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ReviewResponseDTO> getAllReviews(Long courseId) {
        CourseResponseDTO courseDTO = courseService.getCourseById(courseId);
        List<Review> reviews = reviewRepository.findAllByCourse(modelMapper.map(courseDTO, Course.class));
        return reviews.stream().map(review -> modelMapper.map(review, ReviewResponseDTO.class)).toList();
    }

    @Override
    public ReviewResponseDTO getReviewById(Long id) {
        Optional<Review> review = reviewRepository.findByIdAndDeletedFalse(id);
        if (review.isPresent()) {
            return modelMapper.map(review.get(), ReviewResponseDTO.class);
        }
        throw new ReviewNotFoundException();
    }

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewDTO, PublicUserDTO loggedUser) {
        System.out.println(loggedUser);
        if (loggedUser == null) {
            throw new AccessDeniedException();
        }
        try {
            reviewDTO.setId(null);
            userRepository.findByIdAndDeletedFalse(reviewDTO.getUserId()).orElseThrow(UserNotFoundException::new);
            Course course = courseRepository.findByIdAndDeletedFalse(reviewDTO.getCourseId()).orElseThrow(CourseNotFoundException::new);
            Review reviewRequestDTO = modelMapper.map(reviewDTO, Review.class);
            Review review = reviewRepository.save(reviewRequestDTO);
            updateCourseStars(course);
            return modelMapper.map(review, ReviewResponseDTO.class);
        } catch (TransactionException exception) {
            if (exception.getRootCause() instanceof ConstraintViolationException validationException) {
                throw new ValidationBlogException(validationException.getConstraintViolations());
            }
            throw exception;
        }
    }

    @Override
    public void deleteReview(Long id, PublicUserDTO loggedUser) {
        Optional<Review> review = reviewRepository.findByIdAndDeletedFalse(id);
        if (review.isPresent()) {
            if (loggedUser == null || !(Objects.equals(loggedUser.getId(), review.get().getUser().getId()) && !(loggedUser.getRole().equals(Role.ADMIN)))) {
                throw new AccessDeniedException();
            }
            review.get().setDeleted(true);
            reviewRepository.save(review.get());
            updateCourseStars(review.get().getCourse());
        } else {
            throw new ReviewNotFoundException();
        }
    }

    private void updateCourseStars(Course course) {
        List<Review> reviews = reviewRepository.findAllByCourse(course);
        if (reviews.isEmpty()) {
            course.setStars(0);
        } else {
            double totalStars = 0;
            for (Review review : reviews) {
                totalStars += review.getStars();
            }
            double averageStars = totalStars / reviews.size();
            course.setStars(averageStars);
        }
        courseRepository.save(course);
    }
}
