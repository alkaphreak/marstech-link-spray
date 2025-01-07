package fr.marstech.mtlinkspray.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Log
@Service
public class ShortenerService {

    public URL shorten(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            log.severe("Error while shortening URL: " + url);
            throw new RuntimeException(e);
        }
    }
}
