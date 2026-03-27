package com.ColumbusEventAlertService.truncated.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class JsoupService {
    public Connection connect(String url) {
        return Jsoup.connect(url);
    }
    public Document getDocument(Connection connection) throws IOException {
        return connection.get();
    }
}