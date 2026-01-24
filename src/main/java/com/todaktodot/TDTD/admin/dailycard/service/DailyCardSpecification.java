package com.todaktodot.TDTD.admin.dailycard.service;

import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DailyCardSpecification {

    public static Specification<DailyCardEntity> searchCriteria(DailyCardSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

            if (searchDTO.hasMode()) {
                predicates.add(criteriaBuilder.equal(root.get("mode"), searchDTO.getMode()));
            }

            if (searchDTO.hasSubject()) {
                predicates.add(criteriaBuilder.equal(root.get("subject"), searchDTO.getSubject()));
            }

            if (searchDTO.hasType()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), searchDTO.getType()));
            }

            if (searchDTO.hasUseYn()) {
                predicates.add(criteriaBuilder.equal(root.get("useYn"), searchDTO.getUseYn()));
            }

            if (searchDTO.hasKeyword()) {
                String keyword = "%" + searchDTO.getKeyword().trim() + "%";
                predicates.add(criteriaBuilder.like(root.get("cardTitle"), keyword));
            }

            query.orderBy(criteriaBuilder.desc(root.get("regDt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
