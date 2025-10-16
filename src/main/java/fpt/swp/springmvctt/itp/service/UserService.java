package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User save(User userRestriction);
    User update(Long id, User userRestriction);
}
