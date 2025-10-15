package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserRestrictionService {

    List<UserRestriction> findAll();
    UserRestriction findById(Long id);
    UserRestriction save(UserRestriction userRestriction);
    UserRestriction update(Long id, UserRestriction userRestriction);
    boolean delete(Long id);
}
