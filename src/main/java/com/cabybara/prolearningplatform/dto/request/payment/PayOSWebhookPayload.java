package com.cabybara.prolearningplatform.dto.request.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOSWebhookPayload {
    private String code;
    private String desc;
    private boolean success;
    private Data data;
    private String signature;

    @Getter
    @Setter
    public static class Data {
        private long orderCode;
        private int amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String currency;
        private String paymentLinkId;
        private String code;
        private String desc;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String virtualAccountName;
        private String virtualAccountNumber;
    }
}
