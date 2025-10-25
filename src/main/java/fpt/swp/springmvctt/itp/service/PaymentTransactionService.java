package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.PaymentTransaction;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface PaymentTransactionService {

    public Page<PaymentTransaction> findByFilter(String type,
                                                 LocalDate fromDate,
                                                 LocalDate toDate,
                                                 int  page,
                                                 int size
    );
    public PaymentTransaction findById(String id);
}
