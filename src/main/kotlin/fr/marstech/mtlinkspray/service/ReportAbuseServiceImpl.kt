package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.AbuseReportEntity;
import fr.marstech.mtlinkspray.entity.HistoryItem;
import fr.marstech.mtlinkspray.repository.AbuseReportRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportAbuseServiceImpl implements ReportAbuseService {

    private final AbuseReportRepository abuseReportRepository;
    private final MailSenderService mailSenderService;

    public ReportAbuseServiceImpl(
            AbuseReportRepository abuseReportRepository, MailSenderService mailSenderService) {
        this.abuseReportRepository = abuseReportRepository;
        this.mailSenderService = mailSenderService;
    }

    @Override
    public void reportAbuse(String inputAbuseDecsription, HttpServletRequest httpServletRequest) {

        String action = "reportAbuse";
        String report = "%s:%s".formatted(action, inputAbuseDecsription);

        HistoryItem historyItem =
                new HistoryItem(httpServletRequest.getRemoteAddr(), LocalDateTime.now(), action);

        AbuseReportEntity abuseReportEntity =
                new AbuseReportEntity(
                        UUID.randomUUID().toString(),
                        LocalDateTime.now(),
                        null,
                        true,
                        null,
                        Map.of(),
                        new HistoryItem(),
                        List.of());
        abuseReportEntity.setDescription(report);
        abuseReportEntity.setAuthor(historyItem);
        abuseReportEntity = abuseReportRepository.save(abuseReportEntity);

        mailSenderService.sendMail(" MarsTech Link-Spray - Abuse report", abuseReportEntity.toString());
    }
}
