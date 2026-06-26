package com.innovative.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.innovative.entity.*;
import com.innovative.repository.CommentRepository;
import com.innovative.repository.ReviewLikeRepository;
import com.innovative.repository.ReviewRepository;
import com.innovative.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
	private ReviewLikeRepository likeRepository;
	@Autowired
	private CommentRepository commentRepository;

    @PostMapping("/submit-review")
    public String submitReview(@RequestParam String content, Principal principal, RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Review content cannot be empty.");
            return "redirect:/";
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        Review review = new Review();
        review.setContent(content);
        review.setUser(user);
        reviewRepository.save(review);

        return "redirect:/";
    }
    
    @GetMapping("/review")
    public String showReviewForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        Review existingReview = reviewRepository.findByUser(user);
        if (existingReview == null) {
            model.addAttribute("review", new Review());
        } else {
            model.addAttribute("review", existingReview); // pre-fill existing review
        }
        return "review-form"; // Thymeleaf page to edit or submit review
    }
    
    @PostMapping("/review")
    public String postReview(@ModelAttribute Review review) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User loggedInUser = userRepository.findByEmail(email);

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Review existingReview = reviewRepository.findByUser(loggedInUser);
        if (existingReview != null) {
            existingReview.setContent(review.getContent());
            reviewRepository.save(existingReview);
        } else {
            review.setUser(loggedInUser);
            reviewRepository.save(review);
        }

        return "redirect:/";
    }
    
    @PostMapping("/review/{reviewId}/like")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> toggleLike(@PathVariable Long reviewId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        User user = userRepository.findByEmail(principal.getName());
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || user == null) {
            return ResponseEntity.badRequest().body("Review or User not found");
        }

        boolean alreadyLiked = likeRepository.existsByReviewAndUser(review, user);

        if (alreadyLiked) {
            likeRepository.deleteByReviewAndUser(review, user);
        } else {
            ReviewLike like = new ReviewLike();
            like.setReview(review);
            like.setUser(user);
            likeRepository.save(like);
        }

        int likeCount = likeRepository.countByReview(review);
        return ResponseEntity.ok(Map.of("liked", !alreadyLiked, "count", likeCount));
    }
    
    @GetMapping("/review/{reviewId}/like-count")
    @ResponseBody
    public ResponseEntity<?> getLikeStatusAndCount(@PathVariable Long reviewId, Principal principal) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        int likeCount = likeRepository.countByReview(review);
        boolean liked = false;

        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            liked = likeRepository.existsByReviewAndUser(review, user);
        }

        return ResponseEntity.ok(Map.of("liked", liked, "count", likeCount));
    }

    @PostMapping("/review/{reviewId}/comment")
    @ResponseBody
    public ResponseEntity<?> addComment(@PathVariable Long reviewId, @RequestBody Map<String, String> body, Principal principal) {
        String content = body.get("content");
        User user = userRepository.findByEmail(principal.getName());
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || user == null || content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Comment comment = new Comment();
        comment.setReview(review);
        comment.setUser(user);
        comment.setContent(content);
        commentRepository.save(comment);

        return ResponseEntity.ok("Comment added");
    }

    @GetMapping("/review/{reviewId}/comments")
    @ResponseBody
    public ResponseEntity<?> getComments(@PathVariable Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        List<Comment> comments = commentRepository.findByReviewOrderByCreatedAtDesc(review);
        return ResponseEntity.ok(comments);
    }
   
}

