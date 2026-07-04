package com.kgkr.notify.dto;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long userId;
    private Long channelId;
}
