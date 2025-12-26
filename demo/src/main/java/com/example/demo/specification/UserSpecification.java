package com.example.demo.specification;

import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification  {
    public static Specification<UserEntity> hasUsername(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.isEmpty()) return null;
            String pattern = "%" + username.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern);
        };
    }
    public static Specification<UserEntity> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) return null;
            String pattern = "%" + email.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern);
        };
    }
    public static Specification<UserEntity> hasRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.isEmpty()) return null;
            return cb.equal(root.get("role"), role);
        };
    }

}
