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
    public UserRestriction update(Long id, String reason, String status) {
        UserRestriction entity = userRestrictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        entity.setReason(reason);
        entity.setStatus(status);
        entity.setUpdateAt(LocalDate.now());
        return userRestrictionRepository.save(entity);
    }

    @Override
    public void delete(Long id, String username) {
        UserRestriction entity = userRestrictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        if (entity != null) {
            entity.setIsDeleted(true);
            entity.setDeleteBy(username);
            entity.setUpdateAt(LocalDate.now());
            userRestrictionRepository.save(entity);
        }
    }
    @Override
    public Page<UserRestriction> findByFilter(String username, String status, LocalDate fromDate, LocalDate toDate, String deleted, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        Boolean delete = null;
        if (username == null) username = "";
        if ("yes".equals(deleted)) {
            delete = true;
        } else if ("no".equals(deleted)) {
            delete = false;
        }
        if ((status == null || status.isEmpty())) status = null;
        return userRestrictionRepository.findByFilter(username, status, fromDate, toDate, delete, pageable);
    }


}
