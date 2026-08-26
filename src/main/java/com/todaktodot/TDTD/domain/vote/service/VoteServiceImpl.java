package com.todaktodot.TDTD.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.vote.dto.request.*;
import com.todaktodot.TDTD.domain.vote.exception.VoteException;
import com.todaktodot.TDTD.domain.vote.repository.*;
import com.todaktodot.TDTD.domain.vote.repository.entity.*;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteListResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteCursorProjection;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteProjection;
import com.todaktodot.TDTD.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService{

    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectRepository voteSelectRepository;
    private final VoteReportRepository voteReportRepository;
    private final VoteLikeRepository voteLikeRepository;
    private final VoteModerationLogRepository voteModerationLogRepository;
    private final ObjectMapper objectMapper;
    @Override
    @Transactional(readOnly = true)
    public VoteListResponseDTO getList(Long userId, List<VoteCategory> categories, VoteStatus status, String isMineStr, VoteSortCondition sortBy, String cursor, int size) {

        //오늘 생성한 투표 수
        int todayVoteCnt = todayVoteCount(userId);

        Boolean isMine = "Y".equals(isMineStr) ? Boolean.TRUE : null;
//        List<VoteCategory> categoryParams = (categories != null && !categories.isEmpty()) ? categories : List.of(VoteCategory.values());
        List<String> categoryParams = (categories != null && !categories.isEmpty()) ? categories.stream()
                .map(VoteCategory::name).toList() : Arrays.stream(VoteCategory.values()).map(VoteCategory::name).toList();
        //커서 Decode
        VoteCursorDTO cursorDto = null;
        if (StringUtils.hasText(cursor)) {
            cursorDto = decode(cursor);
        }

        List<VoteCursorProjection> voteCursorList = new ArrayList<>();
        //최신순 조회
        //VoteDisplayStatus = HIDDEN 제외, 사용자가 신고한 투표 제외
        if (sortBy.equals(VoteSortCondition.LATEST)) {
            //첫번쨰 페이지 조회
            if (cursorDto == null) {
               voteCursorList =  voteRepository.findFirstByLatest(userId, categoryParams, isMine, status, size+1);
            }
            //다음 페이지 조회
            else {
               voteCursorList =  voteRepository.findNextByLatest(userId, categoryParams, isMine, status, cursorDto.getCreatedAt(), cursorDto.getVoteId(), size+1);
            }
        }
        //인기순 조회
        //VoteDisplayStatus = HIDDEN 제외, 사용자가 신고한 투표 제외
        else if (sortBy.equals(VoteSortCondition.POPULAR)) {
            //첫번쨰 페이지 조회
            if (cursorDto == null) {
                voteCursorList =  voteRepository.findFirstByPopular(userId, categoryParams, isMine, status, size+1);
            }
            //다음 페이지 조회
            else {
                voteCursorList =  voteRepository.findNextByPopular(userId, categoryParams, isMine, status, cursorDto.getParticipantCnt(), cursorDto.getVoteId(), size+1);
            }
        }

        //생성된 투표가 없는 경우
        if (voteCursorList.isEmpty()) {
            return VoteListResponseDTO.builder()
                    .data(Collections.emptyList())
                    .createVoteCnt(todayVoteCnt)
                    .nextCursor(null)
                    .hasNext(false)
                    .build();
        }

        boolean hasNext = voteCursorList.size() > size;
        List<VoteCursorProjection> currentVoteRows = hasNext ? voteCursorList.subList(0, size) : voteCursorList;

        // voteId 추출
        List<Long> voteIds = currentVoteRows.stream()
                .map(VoteCursorProjection::getVoteId)
                .toList();

        //조회한 VoteId List로 옵션 조회
        List<VoteProjection> projections = voteRepository.selectVoteDetails(voteIds, userId);

        //DTO 조립
        List<VoteResponseDTO> voteList = convertVoteResponse(voteIds, projections);

        //다음 Cursor 생성
        String nextCursor = createCursor(sortBy, hasNext, currentVoteRows);

        return VoteListResponseDTO.builder()
                .data(voteList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .createVoteCnt(todayVoteCnt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoteResponseDTO getDetail(Long userId, Long voteId) {
        return getVoteResponse(voteId, userId);
    }

    @Override
    @Transactional
    public VoteCreateResponseDTO create(Long userId, VoteCreateRequestDTO request) {

        int todayVoteCount = todayVoteCount(userId);

        if (todayVoteCount >= 10) {
            throw new VoteException(ErrorCode.VOTE_DAILY_LIMIT_EXCEEDED);
        }

        if (request.getOptions().size() < 2 || request.getOptions().size() > 5) {
            throw new IllegalArgumentException("옵션은 최소 2개, 최대 5개만 등록할 수 있습니다.");
        }

        //vote 저장
        LocalDateTime now = LocalDateTime.now();

        VoteEntity vote = VoteEntity.builder()
                .userId(userId)
                .randomNickname(generateRandomNickname())
                .category(request.getCategory())
                .title(request.getTitle())
                .closedAt(now.plusHours(24))
                .regrId(userId)
                .build();

        VoteEntity savedVote = voteRepository.save(vote);

        //option 저장
        List<VoteOptionEntity> options =
                request.getOptions()
                        .stream()
                        .map(option ->
                                VoteOptionEntity.builder()
                                        .voteId(savedVote.getVoteId())
                                        .sortOrder(option.getOrder())
                                        .content(option.getContent())
                                        .regrId(userId)
                                        .build()
                        )
                        .toList();

        voteOptionRepository.saveAll(options);

        return VoteCreateResponseDTO.builder()
                .voteId(savedVote.getVoteId())
                .build();
    }

    @Override
    @Transactional
    public void update(Long userId, VoteUpdateRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        // 1. 투표 조회
        VoteEntity vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        if (!vote.getUserId().equals(userId)) {
            throw new IllegalStateException("투표 작성자가 아닙니다.");
        }

        // 4. 마감 여부 확인
        if (!vote.getClosedAt().isAfter(now)) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_CLOSED);
        }

        // 5. 이미 참여자가 있는지 확인
        boolean hasParticipant = voteSelectRepository.existsByVoteIdAndDelYn(vote.getVoteId(), "N");
        if (hasParticipant) {
            throw new VoteException(ErrorCode.VOTE_HAS_PARTICIPANTS);
        }

        //투표 본문 수정
        vote.updateVote(request.getCategory(), request.getTitle(), userId);

        //투표 옵션 수정
        //TODO : delete insert 검토
        List<VoteOptionEntity> existingOptions = voteOptionRepository.findAllByVoteIdAndDelYn(vote.getVoteId(), "N");
        existingOptions.forEach(option -> option.deleteOption(userId));

        List<VoteOptionEntity> newOptions = request.getOptions()
                        .stream()
                        .map(option -> VoteOptionEntity.builder()
                                        .voteId(vote.getVoteId())
                                        .sortOrder(option.getOrder())
                                        .content(option.getContent())
                                        .regrId(userId)
                                        .build()
                        ).toList();
        voteOptionRepository.saveAll(newOptions);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long voteId) {
        // 1. 투표 조회
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        if (!vote.getUserId().equals(userId)) {
            throw new IllegalStateException("투표 작성자가 아닙니다.");
        }

        //투표 삭제
        vote.deleteVote(userId);

        //TODO : 옵션만 삭제해도 문제 없는지 검토
        //옵션 삭제
        List<VoteOptionEntity> existingOptions = voteOptionRepository.findAllByVoteIdAndDelYn(vote.getVoteId(), "N");
        existingOptions.forEach(option -> option.deleteOption(userId));

    }

    @Override
    @Transactional
    public VoteResponseDTO select(Long userId, VoteSelectRequestDTO requestDTO) {

        Long voteId = requestDTO.getVoteId();
        Long optionId = requestDTO.getOptionId();

        validateParticipable(voteId);

        // 다른 투표의 옵션 ID를 보내는 요청 방어
        if (!voteOptionRepository.existsByOptionIdAndVoteIdAndDelYn(optionId, voteId, "N")) {
            throw new IllegalArgumentException("해당 투표의 답변 항목이 아닙니다.");
        }

        Optional<VoteSelectEntity> activeSelect =
                voteSelectRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N");

        if (activeSelect.isPresent()) {
            activeSelect.get().updateOptionId(optionId, userId);   // 재투표 - 참여 수는 변하지 않음

        } else {
            voteSelectRepository.save(VoteSelectEntity.builder()
                    .voteId(voteId)
                    .userId(userId)
                    .optionId(optionId)
                    .regrId(userId)
                    .build());

            voteRepository.increaseParticipantCnt(voteId, userId);
        }

        return getVoteResponse(voteId, userId);
    }

    @Override
    @Transactional
    public VoteResponseDTO cancelSelect(Long userId, Long voteId) {

        validateParticipable(voteId);

        // 참여하지 않은 상태에서의 취소 요청은 에러 없이 넘김
        voteSelectRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N")
                .ifPresent(select -> {
                    select.softDelete(userId);
                    voteRepository.decreaseParticipantCnt(voteId, userId);
                });

        return getVoteResponse(voteId, userId);
    }

    @Override
    @Transactional
    public void like(Long userId, Long voteId) {

        validateLikeable(voteId);

        // 이미 좋아요한 상태에서의 재요청은 에러 없이 넘김
        if (voteLikeRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N").isEmpty()) {
            voteLikeRepository.save(VoteLikeEntity.builder()
                    .voteId(voteId)
                    .userId(userId)
                    .regrId(userId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void cancelLike(Long userId, Long voteId) {

        validateLikeable(voteId);

        // 좋아요하지 않은 상태에서의 취소 요청은 에러 없이 넘김
        voteLikeRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N")
                .ifPresent(like -> like.softDelete(userId));
    }

    @Override
    @Transactional
    public void report(Long userId, VoteReportRequestDTO request) {
        //1. 투표 조회
        VoteEntity vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_DELETED);
        }

        // 3. 타유저 투표인지 확인
        if (vote.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 생성한 투표에 신고할 수 없습니다.");
        }

        // 4. 이미 신고한 투표인지 확인
        VoteReportEntity voteReport = voteReportRepository.findByVoteIdAndUserId(request.getVoteId(), userId).orElse(null);
        if (voteReport != null) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_REPORTED);
        }

        VoteReportEntity newReport = VoteReportEntity.builder()
                .voteId(request.getVoteId())
                .reason(request.getReason())
                .userId(userId)
                .regrId(userId)
                .build();

        voteReportRepository.save(newReport);

        List<VoteReportEntity> allReport = voteReportRepository.findAllByVoteId(request.getVoteId());

        // 관리자가 복구/반려로 검토 주기를 초기화한 경우, 그 이전 신고는 누적 집계에서 제외
        long effectiveReportCnt = allReport.stream()
                .filter(r -> vote.getReviewCycleStartedAt() == null || !r.getRegDt().isBefore(vote.getReviewCycleStartedAt()))
                .count();

        //누적 신고 10개 이상인 경우 투표 HIDDEN 처리
        if (effectiveReportCnt >= 10 && vote.getStatus() != VoteDisplayStatus.HIDDEN) {
            String prevStatus = vote.getStatus().name();
            vote.hide(HideReason.AUTO, userId);
            voteModerationLogRepository.save(VoteModerationLogEntity.builder()
                    .voteId(vote.getVoteId())
                    .prevStatus(prevStatus)
                    .newStatus(VoteDisplayStatus.HIDDEN.name())
                    .actor("system")
                    .memo("신고 10건 도달")
                    .regrId(userId)
                    .build());
        }
    }

    /**
     * 당일 생성한 투표 수
     */
    private int todayVoteCount(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        return (int) voteRepository.countByUserIdAndRegDtBetweenAndDelYn(userId, todayStart, tomorrowStart, "N");
    }

    /**
     * 투표 단건 조회
     */
    private VoteResponseDTO getVoteResponse(Long voteId, Long userId) {
        List<VoteProjection> projections = voteRepository.selectVoteDetails(List.of(voteId), userId);

        if (projections.isEmpty()) {
            return null;
        }

        String remainingTime = calculateRemainingTime(projections.get(0).getClosedAt());
        return VoteResponseDTO.from(projections, remainingTime);
    }

    /**
     * 응답 DTO 조립
     */
    private List<VoteResponseDTO> convertVoteResponse(List<Long> voteIds, List<VoteProjection> projections) {
        Map<Long, List<VoteProjection>> projectionMap = projections.stream()
                        .collect(Collectors.groupingBy(VoteProjection::getVoteId));

        List<VoteResponseDTO> result = new ArrayList<>();

        for (Long voteId : voteIds) {
            List<VoteProjection> rows = projectionMap.get(voteId);

            if (rows == null || rows.isEmpty()) {
                continue;
            }

            VoteProjection first = rows.getFirst();

            String remainingTime = calculateRemainingTime(first.getClosedAt());

            result.add(VoteResponseDTO.from(rows, remainingTime));
        }
        return result;
    }

    /**
     * 남은 시간 계산
     */
    private static String calculateRemainingTime(LocalDateTime closedAt) {

        LocalDateTime now = LocalDateTime.now();

        if (!closedAt.isAfter(now)) {
            return "마감";
        }

        long hours = Duration.between(now, closedAt).toHours();
        return String.valueOf(hours);
    }

    /**
     * 다음 커서 생성
     */
    private String createCursor(VoteSortCondition sortBy, boolean hasNext, List<VoteCursorProjection> voteCursorList) {
        String nextCursor = null;
        if (hasNext) {
            VoteCursorProjection last = voteCursorList.getLast();

            VoteCursorDTO next;

            if (sortBy.equals(VoteSortCondition.LATEST)) {
                next = new VoteCursorDTO(VoteSortCondition.LATEST, last.getCreatedAt(), null, last.getVoteId());
            }
            else {
                next = new VoteCursorDTO(VoteSortCondition.POPULAR, null, last.getParticipantCnt(), last.getVoteId());
            }
            nextCursor = encode(next);
        }
        return nextCursor;
    }

    /**
     * 참여 가능한 투표인지 검증
     * 화면에 남은 시간이 표시되어 있더라도 서버 시각을 기준으로 확인
     */
    private void validateParticipable(Long voteId) {

        VoteEntity vote = voteRepository.findByVoteIdForUpdate(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_ALREADY_DELETED));

        if (vote.getStatus() == VoteDisplayStatus.HIDDEN) {
            throw new VoteException(ErrorCode.VOTE_HIDDEN_BY_REPORTS);
        }

        if (!vote.getClosedAt().isAfter(LocalDateTime.now())) {
            throw new VoteException(ErrorCode.VOTE_ALREADY_CLOSED);
        }
    }

    /**
     * 좋아요 가능한 투표인지 검증
     */
    private void validateLikeable(Long voteId) {

        VoteEntity vote = voteRepository.findByVoteIdForUpdate(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_ALREADY_DELETED));

        if (vote.getStatus() == VoteDisplayStatus.HIDDEN) {
            throw new VoteException(ErrorCode.VOTE_HIDDEN_BY_REPORTS);
        }
    }

    /**
     * 랜덤 닉네임 생성
     */
    private String generateRandomNickname() {
        List<String> adjectives = List.of(
                "용감한 ",
                "신나는 ",
                "행복한 ",
                "졸린 ",
                "차분한 ",
                "귀여운 ",
                "활발한 ",
                "엉뚱한 ",
                "똑똑한 ",
                "느긋한 ",
                "용맹한 "
        );

        List<String> animals = List.of(
                "고양이",
                "강아지",
                "수달",
                "토끼",
                "햄스터",
                "여우",
                "판다",
                "코알라",
                "펭귄",
                "알파카",
                "다람쥐",
                "사슴",
                "나무늘보",
                "돌고래"
        );

        ThreadLocalRandom random = ThreadLocalRandom.current();
        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String animal = animals.get(random.nextInt(animals.size()));

        // 0000 ~ 9999
        int number = random.nextInt(10000);

        return adjective + animal + String.format("%04d", number);
    }

    public String encode(VoteCursorDTO cursor) {
        try {
            String json = objectMapper.writeValueAsString(cursor);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new IllegalStateException("투표 커서 생성에 실패했습니다.", e);
        }
    }

    public VoteCursorDTO decode(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);

            String json = new String(decoded, StandardCharsets.UTF_8);

            return objectMapper.readValue(json, VoteCursorDTO.class);

        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 커서입니다.", e);
        }
    }
}
