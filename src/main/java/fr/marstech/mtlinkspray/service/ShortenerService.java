package fr.marstech.mtlinkspray.service;

import java.net.MalformedURLException;
import java.net.URL;

public interface ShortenerService {
    URL shorten(String url) throws MalformedURLException;
}