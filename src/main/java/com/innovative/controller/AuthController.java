package com.innovative.controller;

import com.innovative.entity.*;
import com.innovative.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.innovative.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class AuthController {
	@Autowired
    private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private ReviewLikeRepository likeRepository;

	    @GetMapping("/register")
	    public String showRegistrationForm(Model model) {
	        model.addAttribute("user", new User());
	        return "register"; // register.html
	    }
	    
	    @PostMapping("/register")
	    public String registerUser(@ModelAttribute User user) {
	        userService.saveUser(user);
	        return "redirect:/login?success";
	    }

	 // GET - show index form
	    @GetMapping("/")
	    public String home(Model model) {
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String email = auth.getName(); // this returns the logged-in email

	        User loggedInUser = userRepository.findByEmail(email);

	        if (loggedInUser != null) {
	            model.addAttribute("name", loggedInUser.getName());
	            model.addAttribute("aboutMe", loggedInUser.getAboutMe());

	            Review existingReview = reviewRepository.findByUser(loggedInUser);
	            if (existingReview == null) {
	                existingReview = new Review();
	            }
	            model.addAttribute("review", existingReview);
	        } else {
	            model.addAttribute("name", "Guest");
	            model.addAttribute("aboutMe", "Please log in to post reviews.");
	            model.addAttribute("review", new Review());
	        }

	        List<Review> allReviews = reviewRepository.findAll();
	        Map<Long, Integer> likeCounts = allReviews.stream()
	        	    .collect(Collectors.toMap(
	        	        review -> review.getId(),
	        	        review -> (int) likeRepository.countByReview(review) // explicitly cast to int
	        	    ));

	        model.addAttribute("reviews", allReviews);
		    model.addAttribute("likeCounts", likeCounts);
	        return "index";
	    }
    
	    // GET - show login form
	    @GetMapping("/login")
	    public String showLoginForm() {
	        return "login"; // login.html
	    }
    
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }
    
    @PostMapping("/profile/save")
    public String saveUserProfile(@ModelAttribute("user") User formUser, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return "redirect:/login";
        }

        // Update only editable fields
        existingUser.setName(formUser.getName());
        existingUser.setPhone(formUser.getPhone());
        existingUser.setAboutMe(formUser.getAboutMe());
        existingUser.setEducation(formUser.getEducation());
        existingUser.setCity(formUser.getCity());
        existingUser.setExperience(formUser.getExperience());
        existingUser.setPostalCode(formUser.getPostalCode());
        existingUser.setState(formUser.getState());
        existingUser.setStreet(formUser.getStreet());

        userRepository.save(existingUser);
        return "redirect:/profile";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}

