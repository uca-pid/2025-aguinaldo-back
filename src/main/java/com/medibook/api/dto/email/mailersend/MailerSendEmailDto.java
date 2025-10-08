package com.medibook.api.dto.email.mailersend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailerSendEmailDto {
    
    private From from;
    private List<To> to;
    private String subject;
    private String text;
    private String html;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class From {
        private String email;
        private String name;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class To {
        private String email;
        private String name;
    }
}