package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.PaymentTransaction;
import fpt.swp.springmvctt.itp.repository.PaymentTransactionRepository;
import fpt.swp.springmvctt.itp.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PatymentTransactionServiceImpl implements PaymentTransactionService {

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public Page<PaymentTransaction> findByFilter(String type, String username, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        Boolean delete = null;
        if (type == null) type = "";
        if (username == null) username = "";
        return paymentTransactionRepository.findByFilter(type, username, fromDate, toDate, pageable);
    }

    @Override
    public PaymentTransaction findById(String id) {
        return null;
    }

    public static void main(String[] args) {
        PatymentTransactionServiceImpl pts = new PatymentTransactionServiceImpl();
        PaymentTransaction pt = new PaymentTransaction();
        Page<PaymentTransaction> lpt = pts.findByFilter(null, null, null, null, 0, 10);
        for (PaymentTransaction pay: lpt.getContent()) {
            System.out.println(pay.getId());
        }
    }
}
