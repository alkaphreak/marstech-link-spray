package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ReportAbuseService {

    void reportAbuse(String inputAbuseDecsription, HttpServletRequest httpServletRequest);
}
