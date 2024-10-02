package com.dylibso.examples.kafka.finance;

import java.time.LocalDateTime;
import java.util.StringTokenizer;

public record Price(
        LocalDateTime dateTime,
        double open,
        double high,
        double low,
        double close,
        long volume) {

    /**
     * Parses a row from a well-formatted CSV
     */
    public static Price parseRow(String in) {
        StringTokenizer tok = new StringTokenizer(in, ",;");
        var dateTime = LocalDateTime.parse(tok.nextToken());
        var open = Double.parseDouble(tok.nextToken());
        var high = Double.parseDouble(tok.nextToken());
        var low = Double.parseDouble(tok.nextToken());
        var close = Double.parseDouble(tok.nextToken());
        var volume = Long.parseLong(tok.nextToken());
        return new Price(dateTime, open, high, low, close, volume);
    }

    public String serialize() {
        return String.format("%s,%f,%f,%f,%f,%d", dateTime, open, high, low, close, volume);
    }

}
