package com.dylibso.examples.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class KafkaFilterFetcher {
    private final XTPService xtpService;
    private final String extensionPoint;
    private final String guestKey;

    public KafkaFilterFetcher(XTPService xtpService, String extensionPoint, String guestKey) {
        this.xtpService = xtpService;
        this.extensionPoint = extensionPoint;
        this.guestKey = guestKey;
    }

    public Map<String, XTPService.Extension> extensions() {
        return this.xtpService.fetch(extensionPoint, guestKey);
    }

    public KafkaFilter fetchFilter(XTPService.Extension ext) throws IOException {
        InputStream is = this.xtpService.fetchContent(ext.contentAddress());
        return KafkaFilter.fromInputStream(ext, is);
    }
}
