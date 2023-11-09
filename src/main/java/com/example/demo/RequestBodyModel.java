package com.example.demo;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.qos.logback.classic.net.SyslogAppender;

public class RequestBodyModel {
    private String dbToken;
    private String dbQuery;


    public String getdbToken()
    {
        return dbToken;
    }

    public void setdbToken(String token)
    {
        this.dbToken = token;
    }


    public String getdbQuery()
    {
        return dbQuery;
    }

    public void setdbQuery(String query)
    {
        this.dbQuery = query;
    }
}
