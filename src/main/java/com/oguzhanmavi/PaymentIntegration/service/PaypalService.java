package com.oguzhanmavi.PaymentIntegration.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaypalService {
    private final APIContext apiContext;

    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {
        Amount amount = new Amount()
                .setCurrency(currency)
                .setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));

        Transaction transaction = (Transaction) new Transaction()
                .setDescription(description)
                .setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer().setPaymentMethod(method);

        Payment payment = new Payment()
                .setIntent(intent)
                .setPayer(payer)
                .setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls()
                .setCancelUrl(cancelUrl)
                .setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment().setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }
}
