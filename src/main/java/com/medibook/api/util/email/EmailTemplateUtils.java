package com.medibook.api.util.email;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailTemplateUtils {
    
    public static final String DEFAULT_FONT_FAMILY = "Arial, sans-serif";
    public static final String PRIMARY_COLOR = "#2c5aa0";
    public static final String SUCCESS_COLOR = "#28a745";
    public static final String WARNING_COLOR = "#ffc107";
    public static final String DANGER_COLOR = "#dc3545";
    public static final String LIGHT_BACKGROUND = "#f8f9fa";
    public static final String WARNING_BACKGROUND = "#fff3cd";
    public static final String DANGER_BACKGROUND = "#f8d7da";
    
    public static String createEmailWrapper(String title, String content) {
        return String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="font-family: %s; line-height: 1.6; color: #333; margin: 0; padding: 0;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    %s
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    <p style="font-size: 12px; color: #666; text-align: center;">
                        Este email fue enviado por MediBook. Si tienes preguntas, contacta a nuestro equipo de soporte.
                    </p>
                </div>
            </body>
            </html>
            """, title, DEFAULT_FONT_FAMILY, content);
    }
    
    public static String createInfoBlock(String content, String backgroundColor, String borderColor) {
        return String.format("""
            <div style="background-color: %s; padding: 15px; border-radius: 5px; margin: 20px 0; border: 1px solid %s;">
                %s
            </div>
            """, backgroundColor, borderColor, content);
    }
    
    public static String createTitle(String title, String color) {
        return String.format("<h1 style=\"color: %s;\">%s</h1>", color, title);
    }
    
    public static String createList(String... items) {
        StringBuilder list = new StringBuilder("<ul>");
        for (String item : items) {
            list.append("<li>").append(item).append("</li>");
        }
        list.append("</ul>");
        return list.toString();
    }
    
    public static String createSignature() {
        return "<br><p>Saludos,<br>El equipo de MediBook</p>";
    }
}