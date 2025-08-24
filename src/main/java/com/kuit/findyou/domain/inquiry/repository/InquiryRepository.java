package com.kuit.findyou.domain.inquiry.repository;

import com.kuit.findyou.domain.inquiry.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
