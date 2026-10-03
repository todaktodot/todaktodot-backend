package com.todaktodot.TDTD.domain.vote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.admin.prompt.repository.entity.PromptType;
import com.todaktodot.TDTD.domain.vote.dto.ai.AiGeneratedVoteDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCandidateApproveRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateApproveResultDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.VoteCandidateRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteCandidateServiceImpl implements VoteCandidateService {

    private final ChatClient.Builder chatClientBuilder;
    private final VoteCandidateRepository voteCandidateRepository;
    private final VoteRepository voteRepository;
    private final AiPromptRepository aiPromptRepository;
    private final VoteService voteService;
    private final ObjectMapper objectMapper;

    private static final Long SYSTEM_USER = 0L;
    private static final String AI_MODEL = "gpt-5.4";
    private static final int CANDIDATE_COUNT = 3;
    private static final int RECENT_TITLE_SIZE = 50;

    //VOTE / VOTE_OPTION 컬럼 길이와 VoteServiceImpl.create 의 옵션 개수 제한을 그대로 따른다.
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int OPTION_MAX_LENGTH = 20;
    private static final int OPTION_MIN_COUNT = 2;
    private static final int OPTION_MAX_COUNT = 5;

    @Override
    @Transactional
    public List<VoteCandidateDTO> generate() {

        //중복 회피 재료
        List<String> recentTitles = voteRepository.findRecentTitles(RECENT_TITLE_SIZE);

        AiPromptEntity adminPrompt = findActivePrompt();
        String prompt = buildPrompt(recentTitles, adminPrompt.getPromptContent());

        log.info("=====투표 AI 후보 생성 시작===== 최근 투표 {}건 참고, 프롬프트ID {}", recentTitles.size(), adminPrompt.getPromptId());

        List<AiGeneratedVoteDTO> generated = callAi(prompt);

        String batchKey = LocalDate.now().toString();
        List<VoteCandidateDTO> saved = new ArrayList<>();

        for (AiGeneratedVoteDTO candidate : generated) {
            String reason = validate(candidate, recentTitles);
            if (reason != null) {
                //candidate 자체가 null 일 수 있으므로 제목을 바로 꺼내지 않는다.
                log.warn("후보 제외 - {} / 제목: {}", reason, candidate == null ? "(없음)" : candidate.getTitle());
                continue;
            }

            List<String> options = candidate.getOptions().stream().map(String::trim).toList();

            VoteCandidateEntity entity = VoteCandidateEntity.builder()
                    .batchKey(batchKey)
                    .category(VoteCategory.valueOf(candidate.getCategory()))
                    .title(candidate.getTitle().trim())
                    .optionsJson(writeOptions(options))
                    .promptId(adminPrompt.getPromptId())
                    .aiModel(AI_MODEL)
                    .regrId(SYSTEM_USER)
                    .build();

            saved.add(VoteCandidateDTO.of(voteCandidateRepository.save(entity), options));
        }

        if (saved.isEmpty()) {
            throw new IllegalStateException("검증을 통과한 후보가 없습니다. 프롬프트를 확인해주세요.");
        }

        log.info("=====투표 AI 후보 생성 완료===== {}건 저장", saved.size());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteCandidateDTO> getPendingList() {
        return voteCandidateRepository
                .findAllByStatusAndDelYnOrderByRegDtDescCandidateIdDesc(VoteCandidateStatus.PENDING, "N")
                .stream()
                .map(entity -> VoteCandidateDTO.of(entity, readOptions(entity.getOptionsJson())))
                .toList();
    }

    /**
     * 검증에 걸린 건만 건너뛰고 나머지는 등록한다. (failures 로 사유를 돌려준다)
     * 그 외 예상치 못한 오류는 잡지 않는다 - 전체가 롤백되고 아무것도 등록되지 않는다.
     */
    @Override
    @Transactional
    public VoteCandidateApproveResultDTO approve(List<VoteCandidateApproveRequestDTO> requests, String actor) {

        int approvedCount = 0;
        List<String> failures = new ArrayList<>();
        List<String> approved = new ArrayList<>();

        for (VoteCandidateApproveRequestDTO request : requests) {

            VoteCandidateEntity entity = voteCandidateRepository
                    .findByCandidateIdAndDelYn(request.getCandidateId(), "N")
                    .orElse(null);

            if (entity == null) {
                failures.add(String.format("#%d 후보를 찾을 수 없습니다.", request.getCandidateId()));
                continue;
            }

            //이미 등록했거나 반려한 건은 다시 처리하지 않는다.
            if (entity.getStatus() != VoteCandidateStatus.PENDING) {
                failures.add(String.format("#%d 이미 처리된 후보입니다. (%s)",
                        entity.getCandidateId(), entity.getStatus().getDescription()));
                continue;
            }

            //화면에서 값이 왔으면 비어 있어도 그대로 검증에 넘긴다. 저장분으로 되돌리면 어드민이 지운 문구가 그대로 게시된다.
            String title = request.getTitle() != null ? request.getTitle().trim() : entity.getTitle();
            List<String> options = request.getOptions() != null
                    ? request.getOptions().stream().map(value -> value == null ? "" : value.trim()).toList()
                    : readOptions(entity.getOptionsJson());

            String reason = validateForApprove(title, options);
            if (reason != null) {
                failures.add(String.format("#%d %s", entity.getCandidateId(), reason));
                continue;
            }

            //어드민이 고친 문구를 후보에도 남겨 어떤 내용으로 올렸는지 추적한다.
            entity.updateContent(title, writeOptions(options), SYSTEM_USER);

            VoteCreateResponseDTO created = voteService.createBySystem(toCreateRequest(entity.getCategory(), title, options));
            entity.approve(created.getVoteId(), SYSTEM_USER);
            approvedCount++;
            approved.add(entity.getCandidateId() + "→" + created.getVoteId());
        }

        //커밋 직전에 한 번만 남긴다. 중간에 예외가 나면 이 로그도 남지 않고 전체가 롤백된다.
        if (!approved.isEmpty()) {
            log.info("[Admin] 투표 후보 등록: {}, actor={}", approved, actor);
        }

        return VoteCandidateApproveResultDTO.builder()
                .approvedCount(approvedCount)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional
    public int reject(List<Long> candidateIds, String actor) {

        int rejectedCount = 0;

        for (Long candidateId : candidateIds) {
            VoteCandidateEntity entity = voteCandidateRepository
                    .findByCandidateIdAndDelYn(candidateId, "N")
                    .orElse(null);

            if (entity == null || entity.getStatus() != VoteCandidateStatus.PENDING) {
                continue;
            }

            entity.reject(SYSTEM_USER);
            rejectedCount++;

            log.info("[Admin] 투표 후보 반려: candidateId={}, actor={}", candidateId, actor);
        }

        return rejectedCount;
    }

    private AiPromptEntity findActivePrompt() {
        List<AiPromptEntity> prompts = aiPromptRepository.findLatestActivePerGroupByType(PromptType.VOTE_GENERATION);

        if (prompts.isEmpty()) {
            throw new IllegalStateException("활성화된 투표 생성 프롬프트가 없습니다. 어드민 > 프롬프트 관리에서 등록해주세요.");
        }

        //여러 개면 가장 최근 등록분을 쓴다.
        return prompts.stream()
                .max((a, b) -> Long.compare(a.getPromptId(), b.getPromptId()))
                .orElseThrow();
    }

    private List<AiGeneratedVoteDTO> callAi(String prompt) {

        ChatClient chatClient = chatClientBuilder.build();

        String response = chatClient.prompt()
                .options(OpenAiChatOptions.builder().model(AI_MODEL).build())
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI 응답이 비어 있습니다.");
        }

        log.debug("AI 응답 내용: {}", response);

        try {
            return objectMapper.readValue(extractJsonFromResponse(response), new TypeReference<List<AiGeneratedVoteDTO>>() {});
        } catch (JsonProcessingException e) {
            log.error("AI 응답 파싱 실패: {}", e.getMessage());
            throw new IllegalStateException("AI 응답을 파싱할 수 없습니다", e);
        }
    }

    /**
     * 프롬프트 = 코드 prefix + DB 프롬프트 + 코드 suffix
     * 길이 제한처럼 DB 스키마에 묶인 조건은 어드민이 지울 수 없도록 코드 영역에 둔다.
     */
    private String buildPrompt(List<String> recentTitles, String adminPrompt) {
        return buildSystemPrefix(recentTitles) + "\n\n" + adminPrompt + "\n\n" + buildSystemSuffix();
    }

    private String buildSystemPrefix(List<String> recentTitles) {

        String titleList = recentTitles.isEmpty()
                ? "(아직 등록된 투표가 없습니다)"
                : String.join("\n", recentTitles.stream().map(title -> "- " + title).toList());

        return String.format("""
            너는 커플 앱 '토닥토닥'의 투표 기획자다.
            연인끼리 가볍게 의견이 갈릴 만한 질문을 만든다.

            [최근 등록된 투표 %d건]
            %s

            [조건]
            - 위 목록과 주제가 겹치면 안 된다. 표현만 바꾼 것도 겹치는 것으로 본다.
            - 다만 완전히 동떨어진 주제가 아니라, 위 목록과 결이 이어지는 주제로 만든다.
            - %d개를 서로 다른 카테고리로 만든다.
            """,
                recentTitles.size(),
                titleList,
                CANDIDATE_COUNT
        );
    }

    private String buildSystemSuffix() {

        //enum 에 카테고리가 추가되면 프롬프트에도 자동으로 반영되게 한다.
        String categories = String.join(" / ", Arrays.stream(VoteCategory.values())
                .map(category -> String.format("%s(%s)", category.name(), category.getDescription()))
                .toList());

        return String.format("""
            [반드시 지킬 것]
            - 카테고리는 %s 중 하나
            - 제목은 %d자 이내, 물음표로 끝낸다
            - 선택지는 %d~%d개, 각 선택지는 %d자 이내
            - 선택지끼리 내용이 겹치지 않게, 서로 다른 입장으로 만든다
            - 정답이 있는 질문은 만들지 않는다. 취향이 갈려야 한다
            - 정치, 종교, 외모 비하, 성적인 내용, 특정인 비방 금지

            [출력 형식 - 반드시 아래 JSON 배열로만 응답]
            ```json
            [
              {
                "category": "LOVE",
                "title": "질문 내용?",
                "options": ["선택지1", "선택지2"]
              }
            ]
            ```

            [주의사항]
            - 한국어로 작성
            - JSON 외에 다른 텍스트를 포함하지 말 것
            """, categories, TITLE_MAX_LENGTH, OPTION_MIN_COUNT, OPTION_MAX_COUNT, OPTION_MAX_LENGTH);
    }

    private String extractJsonFromResponse(String response) {
        // 마크다운 코드블록에서 JSON 추출
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        return response.trim();
    }

    /**
     * 생성 직후 검증 - 통과하지 못한 후보는 저장하지 않는다.
     * @return 걸린 사유. 통과면 null
     */
    private String validate(AiGeneratedVoteDTO candidate, List<String> recentTitles) {

        if (candidate == null) {
            return "응답이 비어 있음";
        }

        if (candidate.getCategory() == null || !isValidCategory(candidate.getCategory())) {
            return "카테고리가 LOVE/ECONOMY/LIFESTYLE 중 하나가 아님";
        }

        String title = candidate.getTitle() == null ? "" : candidate.getTitle().trim();
        if (recentTitles.stream().anyMatch(recent -> recent.trim().equals(title))) {
            return "최근 투표와 제목이 완전히 같음";
        }

        return validateForApprove(title, candidate.getOptions());
    }

    /**
     * 게시 직전 검증 - 어드민이 고친 문구도 같은 기준으로 다시 본다.
     * @return 걸린 사유. 통과면 null
     */
    private String validateForApprove(String title, List<String> options) {

        if (!hasText(title) || title.length() > TITLE_MAX_LENGTH) {
            return String.format("제목이 비었거나 %d자를 넘음", TITLE_MAX_LENGTH);
        }

        if (options == null || options.size() < OPTION_MIN_COUNT || options.size() > OPTION_MAX_COUNT) {
            return String.format("선택지가 %d~%d개가 아님", OPTION_MIN_COUNT, OPTION_MAX_COUNT);
        }

        for (String option : options) {
            if (!hasText(option) || option.trim().length() > OPTION_MAX_LENGTH) {
                return String.format("선택지가 비었거나 %d자를 넘음", OPTION_MAX_LENGTH);
            }
        }

        long distinctCount = options.stream().map(String::trim).distinct().count();
        if (distinctCount != options.size()) {
            return "선택지가 서로 중복됨";
        }

        return null;
    }

    private boolean isValidCategory(String category) {
        return Arrays.stream(VoteCategory.values()).anyMatch(value -> value.name().equals(category));
    }

    private VoteCreateRequestDTO toCreateRequest(VoteCategory category, String title, List<String> options) {

        VoteCreateRequestDTO request = new VoteCreateRequestDTO();
        request.setCategory(category);
        request.setTitle(title);

        List<VoteCreateRequestDTO.OptionRequest> optionRequests = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            VoteCreateRequestDTO.OptionRequest option = new VoteCreateRequestDTO.OptionRequest();
            option.setContent(options.get(i));
            option.setOrder(i + 1);
            optionRequests.add(option);
        }
        request.setOptions(optionRequests);

        return request;
    }

    private String writeOptions(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("선택지를 저장할 수 없습니다", e);
        }
    }

    private List<String> readOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("후보 선택지 파싱 실패: {}", optionsJson);
            return List.of();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
