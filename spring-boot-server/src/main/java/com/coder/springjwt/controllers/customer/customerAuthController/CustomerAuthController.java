package com.coder.springjwt.controllers.customer.customerAuthController;

import com.coder.springjwt.constants.customerConstants.customerUrlMappings.CustomerUrlMappings;
import com.coder.springjwt.payload.customerPayloads.customerPayload.CustomerLoginPayload;
import com.coder.springjwt.payload.customerPayloads.customerPayload.FreshSignUpPayload;
import com.coder.springjwt.payload.customerPayloads.freshUserPayload.FreshUserPayload;
import com.coder.springjwt.payload.customerPayloads.freshUserPayload.VerifyMobileOtpPayload;
import com.coder.springjwt.services.customerServices.customerAuthService.CustomerAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(CustomerUrlMappings.CUSTOMER_BASE_URL)
public class CustomerAuthController {

    Logger logger  = LoggerFactory.getLogger(CustomerAuthController.class);

    @Autowired
    private CustomerAuthService customerAuthService;

    @PostMapping(CustomerUrlMappings.CUSTOMER_SIGN_IN)
    public ResponseEntity<?> customerAuthenticateUser(@Validated @RequestBody CustomerLoginPayload customerLoginPayload) {
        return customerAuthService.customerAuthenticateUser(customerLoginPayload);

    }

    @PostMapping(CustomerUrlMappings.CUSTOMER_SIGN_UP)
    public ResponseEntity<?> CustomerSignUp(@Valid @RequestBody FreshUserPayload freshUserPayload) {
        return customerAuthService.CustomerSignUp(freshUserPayload);
    }

    @PostMapping(CustomerUrlMappings.VERIFY_FRESH_USER_MOBILE_OTP)
    public ResponseEntity<?> verifyFreshUserMobileOtp(@Validated @RequestBody VerifyMobileOtpPayload verifyMobileOtpPayload) {
       return customerAuthService.verifyFreshUserMobileOtp(verifyMobileOtpPayload);
    }

    @PostMapping(CustomerUrlMappings.CUSTOMER_SIGN_UP_COMPLETED)
    public ResponseEntity<?> customerSignUpCompleted(@Valid @RequestBody FreshSignUpPayload freshSignUpPayload) {

        return this.customerAuthService.customerSignUpCompleted(freshSignUpPayload);
    }

}