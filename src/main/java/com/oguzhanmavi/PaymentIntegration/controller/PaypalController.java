package com.oguzhanmavi.PaymentIntegration.controller;

import com.oguzhanmavi.PaymentIntegration.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {
    private final PaypalService paypalService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") String amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    ) {
        try {
            String cancelUrl = "https://localhost:8080/payment/cancel";
            String successUrl = "https://localhost:8080/payment/success";
            Payment payment = paypalService.createPayment(Double.valueOf(amount), currency, method, "sale", description, cancelUrl, successUrl);

            for (Links links : payment.getLinks()) {
                if ("approval_url".equals(links.getRel())) {
                    return new RedirectView(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error creating payment: {}", e.getMessage());
            throw new RuntimeException("Error creating payment", e);
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if ("approved".equals(payment.getState())) {
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            log.error("Error executing payment: {}", e.getMessage());
            throw new RuntimeException("Error executing payment", e);
        }
        return "paymentError";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }
}
