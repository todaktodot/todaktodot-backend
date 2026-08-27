package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminVoteDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteSearchCondition;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteStatsDTO;
import com.todaktodot.TDTD.admin.vote.repository.AdminVoteRepository;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import com.todaktodot.TDTD.domain.notification.service.FcmService;
import com.todaktodot.TDTD.domain.vote.repository.VoteLikeRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteModerationLogRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteOptionRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteReportRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteSelectRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.HideReason;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteModerationLogEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteOptionEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteReportEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminVoteServiceImpl implements AdminVoteService {

    private static final Long ADMIN_USER_ID = 0L;

    private final AdminVoteRepository adminVoteRepository;
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectRepository voteSelectRepository;
    private final VoteLikeRepository voteLikeRepository;
    private final VoteReportRepository voteReportRepository;
    private final VoteModerationLogRepository voteModerationLogRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Override
    public Page<AdminVoteListDTO> getList(AdminVoteSearchCondition condition, Pageable pageable) {
        return adminVoteRepository.findList(condition, pageable);
    }

    @Override
    public AdminVoteStatsDTO getStats() {
        return adminVoteRepository.getStats();
    }

    @Override
    public AdminVoteDetailDTO getDetail(Long voteId) {
        VoteEntity vote = voteRepository.findByVoteIdAndDelYn(voteId, "N")
                .orElseThrow(() -> new IllegalArgumentException("투표 정보를 찾을 수 없습니다: " + voteId));

        List<VoteOptionEntity> options = voteOptionRepository.findByVoteIdAndDelYnOrderBySortOrderAsc(voteId, "N");
        Map<Long, Long> selectCntByOptionId = options.stream()
                .collect(Collectors.toMap(VoteOptionEntity::getOptionId,
                        option -> voteSelectRepository.countByOptionIdAndDelYn(option.getOptionId(), "N")));
        long totalSelectCnt = selectCntByOptionId.values().stream().mapToLong(Long::longValue).sum();
        long maxCnt = selectCntByOptionId.values().stream().mapToLong(Long::longValue).max().orElse(0);

        List<AdminVoteDetailDTO.OptionResult> optionResults = options.stream()
                .map(option -> {
                    long cnt = selectCntByOptionId.get(option.getOptionId());
                    int rate = totalSelectCnt == 0 ? 0 : (int) Math.round(cnt * 100.0 / totalSelectCnt);
                    boolean leading = totalSelectCnt > 0 && cnt == maxCnt;
                    return new AdminVoteDetailDTO.OptionResult(option.getOptionId(), option.getContent(), cnt, rate, leading);
                })
                .toList();

        long likeCnt = voteLikeRepository.countByVoteIdAndDelYn(voteId, "N");

        List<VoteReportEntity> reportEntities = voteReportRepository.findAllByVoteIdAndDelYnOrderByRegDtDesc(voteId, "N");
        Map<Long, String> nicknameByUserId = resolveNicknames(reportEntities.stream()
                .map(VoteReportEntity::getUserId)
                .distinct()
                .toList());
        List<AdminVoteDetailDTO.ReportRow> reports = reportEntities.stream()
                .map(r -> new AdminVoteDetailDTO.ReportRow(
                        r.getUserId(),
                        nicknameByUserId.getOrDefault(r.getUserId(), "-"),
                        r.getReason().getDescription(),
                        r.getRegDt()))
                .toList();

        long reportCnt = reportEntities.stream()
                .filter(r -> vote.getReviewCycleStartedAt() == null || !r.getRegDt().isBefore(vote.getReviewCycleStartedAt()))
                .count();

        List<AdminVoteDetailDTO.ModerationLogRow> moderationLogs = voteModerationLogRepository
                .findAllByVoteIdAndDelYnOrderByRegDtDesc(voteId, "N").stream()
                .map(log -> new AdminVoteDetailDTO.ModerationLogRow(
                        log.getRegDt(),
                        log.getActor(),
                        describeChange(log)))
                .toList();

        String voteStatusCode = resolveVoteStatusCode(vote);
        long deleteConfirmedCnt = voteModerationLogRepository.countDeleteConfirmedByAuthor(vote.getUserId());
        // TODO(신고 유저 화면 작업 시): 정지 횟수

        return new AdminVoteDetailDTO(
                vote.getVoteId(),
                vote.getTitle(),
                vote.getCategory().getDescription(),
                voteStatusCode,
                labelOfVoteStatus(voteStatusCode),
                vote.getRandomNickname(),
                vote.getUserId(),
                vote.getRegDt(),
                vote.getClosedAt(),
                calculateRemainingTime(vote.getClosedAt()),
                vote.getParticipantCnt(),
                likeCnt,
                reportCnt,
                deleteConfirmedCnt,
                optionResults,
                reports,
                moderationLogs
        );
    }

    @Override
    @Transactional
    public void hide(Long voteId, String actor) {
        VoteEntity vote = voteRepository.findByVoteIdAndDelYn(voteId, "N")
                .orElseThrow(() -> new IllegalArgumentException("투표 정보를 찾을 수 없습니다: " + voteId));

        String prevStatus = resolveVoteStatusCode(vote);
        vote.hide(HideReason.ADMIN, ADMIN_USER_ID);
        saveLog(voteId, prevStatus, "HIDDEN", actor, "관리자 수동 숨김");
        fcmService.sendToUser(vote.getUserId(), PushMessage.voteHidden(vote.getVoteId(), vote.getTitle()));
    }

    @Override
    @Transactional
    public void restore(Long voteId, String actor) {
        VoteEntity vote = voteRepository.findByVoteIdAndDelYn(voteId, "N")
                .orElseThrow(() -> new IllegalArgumentException("투표 정보를 찾을 수 없습니다: " + voteId));

        String prevStatus = resolveVoteStatusCode(vote);
        vote.restore(ADMIN_USER_ID);
        saveLog(voteId, prevStatus, resolveVoteStatusCode(vote), actor, "관리자 복구");
    }

    @Override
    @Transactional
    public void delete(Long voteId, String actor) {
        VoteEntity vote = voteRepository.findByVoteIdAndDelYn(voteId, "N")
                .orElseThrow(() -> new IllegalArgumentException("투표 정보를 찾을 수 없습니다: " + voteId));

        String prevStatus = resolveVoteStatusCode(vote);
        vote.deleteVote(ADMIN_USER_ID);
        saveLog(voteId, prevStatus, "DELETED", actor, "관리자 삭제");
    }

    private void saveLog(Long voteId, String prevStatus, String newStatus, String actor, String memo) {
        voteModerationLogRepository.save(VoteModerationLogEntity.builder()
                .voteId(voteId)
                .prevStatus(prevStatus)
                .newStatus(newStatus)
                .actor(actor)
                .memo(memo)
                .regrId(ADMIN_USER_ID)
                .build());
    }

    private String describeChange(VoteModerationLogEntity log) {
        String prev = log.getPrevStatus() == null ? "등록" : log.getPrevStatus();
        return prev + " → " + log.getNewStatus() + (log.getMemo() != null ? " (" + log.getMemo() + ")" : "");
    }

    private Map<Long, String> resolveNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickname() == null ? "-" : u.getNickname()));
    }

    private String resolveVoteStatusCode(VoteEntity vote) {
        if (vote.getStatus() == com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus.HIDDEN) {
            return vote.getHideReason() == HideReason.AUTO ? "AUTO_HIDDEN" : "HIDDEN";
        }
        return vote.getClosedAt().isAfter(LocalDateTime.now()) ? "ACTIVE" : "CLOSED";
    }

    private String labelOfVoteStatus(String code) {
        return switch (code) {
            case "ACTIVE" -> "진행중";
            case "CLOSED" -> "마감";
            case "AUTO_HIDDEN" -> "자동숨김";
            case "HIDDEN" -> "숨김";
            default -> code;
        };
    }

    // [TDTDBE-144] VoteSelectServiceImpl.calculateRemainingTime 과 동일한 포맷(시간 단위, 마감 시 "마감")으로 통일
    private static String calculateRemainingTime(LocalDateTime closedAt) {
        LocalDateTime now = LocalDateTime.now();
        if (!closedAt.isAfter(now)) {
            return "마감";
        }
        long hours = Duration.between(now, closedAt).toHours();
        return String.valueOf(hours);
    }
}
