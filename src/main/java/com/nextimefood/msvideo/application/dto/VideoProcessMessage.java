package com.nextimefood.msvideo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VideoProcessMessage {

    @JsonProperty("Records")
    private List<Record> records;

    public VideoProcessMessage() {
    }

    public VideoProcessMessage(String bucket, String key) {
        this.records = List.of(new Record(new S3(new Bucket(bucket), new ObjectInfo(key))));
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public static class Record {
        private S3 s3;

        public Record() {}

        public Record(S3 s3) {
            this.s3 = s3;
        }

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }
    }

    public static class S3 {
        private Bucket bucket;
        private ObjectInfo object;

        public S3() {}

        public S3(Bucket bucket, ObjectInfo object) {
            this.bucket = bucket;
            this.object = object;
        }

        public Bucket getBucket() {
            return bucket;
        }

        public void setBucket(Bucket bucket) {
            this.bucket = bucket;
        }

        public ObjectInfo getObject() {
            return object;
        }

        public void setObject(ObjectInfo object) {
            this.object = object;
        }
    }

    public static class Bucket {
        private String name;

        public Bucket() {}

        public Bucket(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ObjectInfo {
        private String key;

        public ObjectInfo() {}

        public ObjectInfo(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
