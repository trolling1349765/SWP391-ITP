package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.repository.UserRestrictionRepository;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<UserRestriction> findByFilter(String username, String status, LocalDate fromDate, LocalDate toDate) {
        if ((status == null || status.isEmpty())) {
            if (fromDate == null && toDate == null) return userRestrictionRepository.findAll();
            if (fromDate != null && toDate != null) return userRestrictionRepository.findByFilter(null, fromDate, toDate);
        }
        return userRestrictionRepository.findByFilter(username, status, fromDate, toDate);
    }


}
