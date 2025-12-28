package com.todaktodot.TDTD.domain.term.repository;

import com.todaktodot.TDTD.domain.term.repository.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
}
