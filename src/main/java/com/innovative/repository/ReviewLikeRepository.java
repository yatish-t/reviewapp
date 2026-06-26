package com.innovative.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.innovative.entity.*;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    int countByReview(Review review);
    boolean existsByReviewAndUser(Review review, User user);
    void deleteByReviewAndUser(Review review, User user);
}
