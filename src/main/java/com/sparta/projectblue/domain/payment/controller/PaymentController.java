package com.sparta.projectblue.domain.payment.controller;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.payment.dto.PaymentResponseDto;
import com.sparta.projectblue.domain.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody)
            throws Exception {

        return ResponseEntity.ok(paymentService.confirmPayment(jsonBody));
    }

    // 결제 금액이 100원 이상인 경우 TossPayments 사용
    @GetMapping("/payments/{reservationId}")
    public String payments(
            Model model,
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long couponId) {

        PaymentResponseDto responseDto = paymentService.setValue(reservationId, couponId);

        model.addAttribute("paymentResponse", responseDto);

        return "/checkout";
    }

    // 결제 금액이 0원인 경우 TossPayments 사용 불가능
    @PostMapping("/payments/{reservationId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> payments(
            @PathVariable Long reservationId, @RequestParam(required = false) Long couponId) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.freePay(reservationId, couponId)));
    }

    @GetMapping("/payments/success")
    public String paymentRequest(HttpServletRequest request, Model model) throws Exception {

        return "/success";
    }

    @GetMapping("/fail")
    public String failPayment(HttpServletRequest request, Model model) throws Exception {

        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "/fail";
    }
}
