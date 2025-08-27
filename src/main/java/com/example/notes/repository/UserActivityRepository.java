package com.example.notes.repository;

import com.example.notes.model.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {
}


