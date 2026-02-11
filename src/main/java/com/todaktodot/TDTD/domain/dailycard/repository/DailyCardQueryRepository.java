package com.todaktodot.TDTD.domain.dailycard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardListDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QDailyCardQuestionEntity;
import com.todaktodot.TDTD.domain.login.respository.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyCardQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QDailyCardEntity card = QDailyCardEntity.dailyCardEntity;
    private static final QDailyCardQuestionEntity question = QDailyCardQuestionEntity.dailyCardQuestionEntity;
    private static final QUser regr = new QUser("regr");  // 등록자
    private static final QUser updr = new QUser("updr");  // 수정자

    /**
     * 데일리카드 목록 검색
     */
    public Page<DailyCardListDTO> searchDailyCards(DailyCardSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());

        BooleanBuilder where = buildWhereClause(searchDTO);

        var questionCountSubQuery = JPAExpressions
                .select(question.count())
                .from(question)
                .where(question.cardId.eq(card.cardId)
                        .and(question.delYn.eq("N")));

        var regrNameSubQuery = JPAExpressions
                .select(regr.nickname)
                .from(regr)
                .where(regr.id.eq(card.regrId));

        var updrNameSubQuery = JPAExpressions
                .select(updr.nickname)
                .from(updr)
                .where(updr.id.eq(card.updrId));

        List<DailyCardListDTO> results = queryFactory
                .select(Projections.constructor(DailyCardListDTO.class,
                        card.cardId,
                        card.mode,
                        card.subject,
                        card.type,
                        card.cardTitle,
                        card.situation,
                        ExpressionUtils.as(questionCountSubQuery, "questionCount"),
                        card.useYn,
                        card.regDt,
                        card.regrId,
                        ExpressionUtils.as(regrNameSubQuery, "regrNm"),
                        card.updDt,
                        card.updrId,
                        ExpressionUtils.as(updrNameSubQuery, "updrNm"),
                        Expressions.constant(0)
                ))
                .from(card)
                .where(where)
                .orderBy(card.regDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(card.count())
                .from(card)
                .where(where)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildWhereClause(DailyCardSearchDTO searchDTO) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(card.delYn.eq("N"));

        if (searchDTO.hasMode()) {
            where.and(card.mode.eq(searchDTO.getMode()));
        }

        if (searchDTO.hasSubject()) {
            where.and(card.subject.eq(searchDTO.getSubject()));
        }

        if (searchDTO.hasType()) {
            where.and(card.type.eq(searchDTO.getType()));
        }

        if (searchDTO.hasUseYn()) {
            where.and(card.useYn.eq(searchDTO.getUseYn()));
        }

        if (searchDTO.hasKeyword()) {
            where.and(card.cardTitle.contains(searchDTO.getKeyword()));
        }

        return where;
    }
}
