package com.todaktodot.TDTD.batch.report;

import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateDTO;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService.DiscordEmbedField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VoteCandidateReportFormatter {

    private static final String ADMIN_PATH = "/admin/vote-candidate";

    //어드민 주소가 없으면 링크 없이 발송. 설정이 없어도 알림은 유지
    @Value("${admin.base-url:}")
    private String adminBaseUrl;

    public String buildDescription(List<VoteCandidateDTO> candidates) {

        String summary = String.format("후보 **%d개**가 준비됐습니다. 확인 후 등록해주세요.", candidates.size());

        if (adminBaseUrl == null || adminBaseUrl.isBlank()) {
            return summary;
        }

        return summary + String.format("\n\n👉 [어드민에서 확인하기](%s%s)", adminBaseUrl.replaceAll("/+$", ""), ADMIN_PATH);
    }

    public List<DiscordEmbedField> buildFields(List<VoteCandidateDTO> candidates) {
        return candidates.stream()
                .map(candidate -> new DiscordEmbedField(
                        String.format("[%s] %s", candidate.getCategoryLabel(), candidate.getTitle()),
                        String.join(" / ", candidate.getOptions()),
                        false
                ))
                .toList();
    }
}
