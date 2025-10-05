package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.UserRestriction;
import fpt.swp.springmvctt.itp.repository.UserRestrictionRepository;
import fpt.swp.springmvctt.itp.service.UserRestrictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
