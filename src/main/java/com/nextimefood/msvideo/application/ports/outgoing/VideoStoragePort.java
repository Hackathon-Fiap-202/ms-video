package com.nextimefood.msvideo.application.ports.outgoing;

import java.io.IOException;
import java.io.InputStream;

public interface VideoStoragePort {

    void upload(String bucket, String key, InputStream content) throws IOException;

}
