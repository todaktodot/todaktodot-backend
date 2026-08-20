package com.todaktodot.TDTD.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteOptionEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteReportEntity;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCursorDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteReportRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteUpdateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteListResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.VoteOptionRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteReportRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteSelectRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSortCondition;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteCursorProjection;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteProjection;
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
    private final ObjectMapper objectMapper;
    @Override
    @Transactional(readOnly = true)
    public VoteListResponseDTO getList(Long userId, List<VoteCategory> categories, VoteStatus status, String isMineStr, VoteSortCondition sortBy, String cursor, int size) {

        //오늘 생성한 투표 수
        int todayVoteCnt = todayVoteCount(userId);

        Boolean isMine = "Y".equals(isMineStr) ? Boolean.TRUE : null;
        List<VoteCategory> categoryParams = (categories != null && !categories.isEmpty()) ? categories : List.of(VoteCategory.values());

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
        List<Long> voteIds = List.of(voteId);

        //VoteId List로 옵션 조회
        List<VoteProjection> projections = voteRepository.selectVoteDetails(voteIds, userId);

        //DTO 조립
        List<VoteResponseDTO> voteList = convertVoteResponse(voteIds, projections);

        if (voteList.isEmpty()) {
            return null;
        }

        return voteList.getFirst();
    }

    @Override
    @Transactional
    public VoteCreateResponseDTO create(Long userId, VoteCreateRequestDTO request) {
        int todayVoteCount = todayVoteCount(userId);

        //TODO: 당일 생성 가능 투표 수 제한 예외처리
        if (todayVoteCount >= 10) {
            throw new IllegalStateException("더 이상 투표를 생성할 수 없습니다.");
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
        //TODO: 예외처리 필요
        // 1. 투표 조회
        VoteEntity vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 투표입니다."));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new IllegalStateException("이미 삭제된 투표입니다.");
        }

        // 3. 작성자 본인 확인
        if (!vote.getUserId().equals(userId)) {
            throw new IllegalStateException("투표 작성자가 아닙니다.");
        }

        // 4. 마감 여부 확인
        if (!vote.getClosedAt().isAfter(now)) {
            throw new IllegalStateException("이미 마감된 투표입니다.");
        }

        // 5. 이미 참여자가 있는지 확인
        boolean hasParticipant = voteSelectRepository.existsByVoteIdAndDelYn(vote.getVoteId(), "N");
        if (hasParticipant) {
            throw new IllegalStateException("이미 참여자가 존재합니다.");
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
        // TODO: 예외처리 필요
        // 1. 투표 조회
        VoteEntity vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 투표입니다."));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new IllegalStateException("이미 삭제된 투표입니다.");
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
    public void report(Long userId, VoteReportRequestDTO request) {
        //TODO: 예외처리 필요
        //1. 투표 조회
        VoteEntity vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 투표입니다."));

        // 2. 삭제된 투표인지 확인
        if ("Y".equals(vote.getDelYn())) {
            throw new IllegalStateException("이미 삭제된 투표입니다.");
        }

        // 3. 타유저 투표인지 확인
        if (vote.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 생성한 투표에 신고할 수 없습니다.");
        }

        // 4. 이미 신고한 투표인지 확인
        VoteReportEntity voteReport = voteReportRepository.findByVoteIdAndUserId(request.getVoteId(), userId).orElse(null);
        if (voteReport != null) {
            throw new IllegalStateException("이미 신고한 투표입니다.");
        }

        VoteReportEntity newReport = VoteReportEntity.builder()
                .voteId(request.getVoteId())
                .reason(request.getReason())
                .userId(userId)
                .regrId(userId)
                .build();

        voteReportRepository.save(newReport);

        List<VoteReportEntity> allReport = voteReportRepository.findAllByVoteId(request.getVoteId());

        //누적 신고 10개 이상인 경우 투표 HIDDEN 처리
        if (allReport.size() >= 10) {
            vote.updateDisplayStatus(VoteDisplayStatus.HIDDEN, userId);
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
     * TODO 잔여 시간은 시간 단위 기준 (분 미표기), 마감 시 "마감" 고정 표기 — UX 정책 화면 확인
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
                "느긋한 "
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
                "사슴"
        );

        ThreadLocalRandom random = ThreadLocalRandom.current();

        String adjective = adjectives.get(random.nextInt(adjectives.size()));

        String animal = animals.get(random.nextInt(animals.size()));

        return adjective + animal;
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
