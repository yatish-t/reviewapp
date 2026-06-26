package com.innovative.repository;

import com.innovative.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByOrderByIdDesc();
    
    Review findByUser(User user);
}
