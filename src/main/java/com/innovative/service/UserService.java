package com.innovative.service;

import com.innovative.entity.User;

public interface UserService {
	
	
    void saveUser(User user);
    User findByEmail(String email);
}
