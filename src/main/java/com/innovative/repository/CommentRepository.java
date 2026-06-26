package com.innovative.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.innovative.entity.*;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewOrderByCreatedAtDesc(Review review);
}
