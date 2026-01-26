package com.todaktodot.TDTD.admin.dailycardassign.service;

import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistoryDTO;
import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistorySearchDTO;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDailyCardAssignServiceImpl implements AdminDailyCardAssignService {

    private final CoupleDailyCardRepository coupleDailyCardRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    @Override
    public List<AssignmentHistoryDTO> getAssignmentHistory(AssignmentHistorySearchDTO searchDTO, int limit) {
        List<CoupleDailyCardEntity> assignments;
        if (searchDTO != null
                && searchDTO.getIssuedStartDate() != null
                && searchDTO.getIssuedEndDate() != null) {
            assignments = coupleDailyCardRepository
                    .findAssignmentHistory(searchDTO.getIssuedStartDate(), searchDTO.getIssuedEndDate());
        } else {
            assignments = coupleDailyCardRepository
                    .findRecentAssignments(limit);
        }

        List<AssignmentHistoryDTO> histories = mapToHistoryDTOs(assignments);
        return applyFilters(histories, searchDTO);
    }

    @Override
    public List<AssignmentHistoryDTO> getRecentAssignments(int limit) {
        List<CoupleDailyCardEntity> assignments = coupleDailyCardRepository
                .findRecentAssignments(limit);
        return mapToHistoryDTOs(assignments);
    }

    private List<AssignmentHistoryDTO> applyFilters(List<AssignmentHistoryDTO> histories,
                                                    AssignmentHistorySearchDTO searchDTO) {
        if (searchDTO == null || !searchDTO.hasAnyFilter()) {
            return histories;
        }

        return histories.stream()
                .filter(history -> matchesText(history.getCoupleName(), searchDTO.getCoupleName()))
                .filter(history -> matchesText(history.getCardTitle(), searchDTO.getCardTitle()))
                .filter(history -> matchesEnum(history.getCardMode(), searchDTO.getCardMode()))
                .filter(history -> matchesEnum(history.getCardSubject(), searchDTO.getCardSubject()))
                .filter(history -> matchesEnum(history.getCardType(), searchDTO.getCardType()))
                .filter(history -> matchesDateRange(history.getIssuedDate(),
                        searchDTO.getIssuedStartDate(), searchDTO.getIssuedEndDate()))
                .filter(history -> matchesDateRange(history.getRegDt() != null ? history.getRegDt().toLocalDate() : null,
                        searchDTO.getRegStartDate(), searchDTO.getRegEndDate()))
                .toList();
    }

    private boolean matchesText(String source, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (source == null) {
            return false;
        }
        return source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private boolean matchesEnum(String source, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        if (source == null) {
            return false;
        }
        return source.equalsIgnoreCase(filter.trim());
    }

    private boolean matchesDateRange(LocalDate target, LocalDate start, LocalDate end) {
        if (target == null) {
            return false;
        }
        if (start != null && target.isBefore(start)) {
            return false;
        }
        return end == null || !target.isAfter(end);
    }

    private List<AssignmentHistoryDTO> mapToHistoryDTOs(List<CoupleDailyCardEntity> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        // 커플 ID 추출
        Set<Long> coupleIds = assignments.stream()
                .map(CoupleDailyCardEntity::getCoupleId)
                .collect(Collectors.toSet());

        // 커플 정보 배치 조회
        Map<Long, CoupleEntity> coupleMap = coupleRepository.findAllById(coupleIds).stream()
                .collect(Collectors.toMap(CoupleEntity::getCoupleId, c -> c));

        // 유저 ID 추출
        Set<Long> userIds = coupleMap.values().stream()
                .flatMap(c -> java.util.stream.Stream.of(c.getUserId1(), c.getUserId2()))
                .collect(Collectors.toSet());

        // 유저 정보 배치 조회
        Map<Long, String> userNameMap = userRepository.findByIdIn(new ArrayList<>(userIds)).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user.getName() != null ? user.getName() : "?",
                        (a, b) -> a
                ));

        // DTO 매핑
        return assignments.stream()
                .map(assignment -> {
                    CoupleEntity couple = coupleMap.get(assignment.getCoupleId());
                    DailyCardEntity dailyCard = assignment.getDailyCard();

                    String user1Name = couple != null ? userNameMap.get(couple.getUserId1()) : null;
                    String user2Name = couple != null ? userNameMap.get(couple.getUserId2()) : null;

                    return AssignmentHistoryDTO.builder()
                            .coupleCardId(assignment.getCoupleCardId())
                            .coupleId(assignment.getCoupleId())
                            .user1Name(user1Name)
                            .user2Name(user2Name)
                            .issuedDate(assignment.getIssuedDate())
                            .cardId(assignment.getCardId())
                            .cardTitle(dailyCard != null ? dailyCard.getCardTitle() : "-")
                            .cardMode(dailyCard != null ? dailyCard.getMode().name() : null)
                            .cardModeDisplayName(dailyCard != null ? dailyCard.getMode().getDisplayName() : "-")
                            .cardSubject(dailyCard != null ? dailyCard.getSubject().name() : null)
                            .cardSubjectDisplayName(dailyCard != null ? dailyCard.getSubject().getDisplayName() : "-")
                            .cardType(dailyCard != null ? dailyCard.getType().name() : "-")
                            .cardTypeDisplayName(dailyCard != null ? dailyCard.getType().getDisplayName() : "-")
                            .regDt(assignment.getRegDt())
                            .build();
                })
                .toList();
    }
}
