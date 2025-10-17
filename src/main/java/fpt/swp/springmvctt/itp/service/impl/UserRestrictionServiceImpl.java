package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.repository.UserRestrictionRepository;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserRestrictionServiceImpl implements UserRestrictionService {

    @Autowired
    private UserRestrictionRepository userRestrictionRepository;

    @Override
    public List<UserRestriction> findAll() {

        return userRestrictionRepository.findAll();
    }

    @Override
    public UserRestriction findById(Long id) {
        return userRestrictionRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    @Override
    public UserRestriction save(UserRestriction userRestriction) {
        return userRestrictionRepository.save(userRestriction);
    }

    @Override
    public UserRestriction update(Long id, UserRestriction updated) {
        UserRestriction entity = userRestrictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        entity.setReason(updated.getReason());
        entity.setStatus(updated.getStatus());
        entity.setUpdateAt(LocalDateTime.now());
        return userRestrictionRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        UserRestriction entity = userRestrictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

    }
    @Override
    public Page<UserRestriction> findByFilter(String username, String status, LocalDate fromDate, LocalDate toDate, String deleted, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        if (username == null) username = "";
        if ((status == null || status.isEmpty())) {
            if (fromDate == null && toDate == null && deleted.equals("all"))
                return userRestrictionRepository
                        .findByFilter(username, null, null, null, pageable);
            if (fromDate != null && toDate != null && !deleted.equals("all"))
                return userRestrictionRepository
                        .findByFilter(username,null, fromDate, toDate, deleted.equals("yes"), pageable);
        }
        return userRestrictionRepository.findByFilter(username, status, fromDate, toDate, null, pageable);
    }


}
