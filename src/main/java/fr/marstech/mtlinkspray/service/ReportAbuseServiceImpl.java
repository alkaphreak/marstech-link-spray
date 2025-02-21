package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.AbuseReport;
import fr.marstech.mtlinkspray.repository.AbuseReportRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Log
@Service
public class ReportAbuseServiceImpl implements ReportAbuseService {

    private final AbuseReportRepository abuseReportRepository;
    private final MailSenderService mailSenderService;

    public ReportAbuseServiceImpl(AbuseReportRepository abuseReportRepository, MailSenderService mailSenderService) {
        this.abuseReportRepository = abuseReportRepository;
        this.mailSenderService = mailSenderService;
    }

    @Override
    public void reportAbuse(String inputAbuseDecsription) {

        log.info("reportAbuse:%s".formatted(inputAbuseDecsription));

        AbuseReport abuseReport = new AbuseReport();
        abuseReport.setAbuseDescription(inputAbuseDecsription);
        abuseReport = abuseReportRepository.save(abuseReport);

        mailSenderService.sendMail(" MarsTech Link-Spray - Abuse report", abuseReport.toString());
    }
}
