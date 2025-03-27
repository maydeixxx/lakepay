package com.lakepayProj.chatService;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {

    private String content;
    private String sender;

}