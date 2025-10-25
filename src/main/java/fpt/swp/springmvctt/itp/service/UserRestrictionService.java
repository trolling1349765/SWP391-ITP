package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


public interface UserRestrictionService {

    List<UserRestriction> findAll();
    UserRestriction findById(Long id);
    UserRestriction save(UserRestriction userRestriction);
    UserRestriction update(Long id, String reason, String status);
    void delete(Long id, String username);
    Page<UserRestriction> findByFilter(
            String username,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String deleted,
            int  page,
            int size
    );
}
