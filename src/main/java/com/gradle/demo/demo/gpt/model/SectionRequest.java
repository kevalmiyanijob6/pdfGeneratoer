package com.gradle.demo.demo.gpt.model;


import java.util.List;

public class SectionRequest {

    private String tag;
    private List<FieldRequest> fields;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<FieldRequest> getFields() {
        return fields;
    }

    public void setFields(List<FieldRequest> fields) {
        this.fields = fields;
    }
}
