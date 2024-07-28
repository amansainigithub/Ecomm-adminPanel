package com.coder.springjwt.services.customerServices.customerAuthService.imple;

import com.coder.springjwt.controllers.customer.customerAuthController.CustomerAuthController;
import com.coder.springjwt.exception.customerException.InvalidMobileNumberException;
import com.coder.springjwt.exception.customerException.InvalidUsernameAndPasswordException;
import com.coder.springjwt.helpers.generateRandomNumbers.GenerateMobileOTP;
import com.coder.springjwt.helpers.ValidateMobNumber.ValidateMobileNumber;
import com.coder.springjwt.models.ERole;
import com.coder.springjwt.models.Role;
import com.coder.springjwt.models.User;
import com.coder.springjwt.payload.customerPayloads.customerPayload.CustomerLoginPayload;
import com.coder.springjwt.payload.customerPayloads.customerPayload.FreshSignUpPayload;
import com.coder.springjwt.payload.customerPayloads.freshUserPayload.FreshUserPayload;
import com.coder.springjwt.payload.customerPayloads.freshUserPayload.VerifyMobileOtpPayload;
import com.coder.springjwt.payload.response.JwtResponse;
import com.coder.springjwt.util.MessageResponse;
import com.coder.springjwt.repository.RoleRepository;
import com.coder.springjwt.repository.UserRepository;
import com.coder.springjwt.security.jwt.JwtUtils;
import com.coder.springjwt.security.services.UserDetailsImpl;
import com.coder.springjwt.services.MobileOtpService.MobileOtpService;
import com.coder.springjwt.services.customerServices.customerAuthService.CustomerAuthService;
import com.coder.springjwt.services.emailServices.simpleEmailService.SimpleEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomerAuthServiceImple implements CustomerAuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SimpleEmailService simpleEmailService;

    @Autowired
    private MobileOtpService mobileOtpService;


    Logger logger  = LoggerFactory.getLogger(CustomerAuthController.class);


    @Override
    public ResponseEntity<?> customerAuthenticateUser(CustomerLoginPayload customerLoginPayload) {
        if(customerLoginPayload.getUserrole().equals("ROLE_CUSTOMER"))
        {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(customerLoginPayload.getUsername(), customerLoginPayload.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            for(String role : roles){
                if(role.equals("ROLE_CUSTOMER")){
                    return ResponseEntity.ok(new JwtResponse(jwt,
                            userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            roles));
                }
            }
        } else{
            logger.error("CustomerAuthService :: " + "Unauthorized User ==> " + customerLoginPayload.getUsername() );
            throw new InvalidUsernameAndPasswordException("Invalid UserName and Password!");
        }
        return ResponseEntity.badRequest().body("Error: Unauthorized");
    }

    @Override
    public ResponseEntity<?> CustomerSignUp(FreshUserPayload freshUserPayload) {
        //Validate Mobile Number
        //FreshUserPayload Parameter Role Is Not Mandatory------
        if(!ValidateMobileNumber.isValid(freshUserPayload.getUsername())) {
            throw new InvalidMobileNumberException("Invalid Mobile Number");
        }

        if (userRepository.existsByUsername(freshUserPayload.getUsername())) {
            User user = userRepository.findByUsername(freshUserPayload.getUsername()).get();

            if(user.getRegistrationCompleted().equals("Y") && user.getIsMobileVerify().equals("Y"))
            {
                return ResponseEntity
                        .ok()
                        .body(new MessageResponse("FLY_LOGIN_PAGE",HttpStatus.OK));
            }else{
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!",HttpStatus.BAD_REQUEST));
            }

        }else {
            // Create new user's account
            User user = new User(freshUserPayload.getUsername(),
                    freshUserPayload.getUsername() + "no@gmail.com",
                    encoder.encode("password"),
                    freshUserPayload.getUsername());

            //Set Project Role
            user.setProjectRole(ERole.ROLE_CUSTOMER.toString());

            //User Set Username or mobile
            user.setMobile(freshUserPayload.getUsername());

            //Set Mobile OTP Verified
            user.setIsMobileVerify("N");
            //Set Reg Completed
            user.setRegistrationCompleted("N");

            //Mobile OTP Generator [Mobile]
            String otp = GenerateMobileOTP.generateOtp(6);

            logger.info("OTP SUCCESSFULLY GENERATED :: " + otp);

            //mobileOtpService.sendSMS(otp,freshUserPayload.getUsername());
            user.setMobileOtp(otp);

            //Set<String> strRoles = customerSignUpRequest.getRole();
            Set<Role> roles = new HashSet<>();
            Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_CUSTOMER NOT FOUND."));
            roles.add(customerRole);

            user.setRoles(roles);

            userRepository.save(user);
            logger.info("FRESH USER CREATED SUCCESSFULLY");
            return ResponseEntity.ok(new MessageResponse("FRESH USER CREATED SUCCESSFULLY",HttpStatus.OK));
        }
    }

    @Override
    public ResponseEntity<?> verifyFreshUserMobileOtp(VerifyMobileOtpPayload verifyMobileOtpPayload) {
        MessageResponse response = new MessageResponse();

        User user =  this.userRepository.findByUsername
                (verifyMobileOtpPayload.getUsername()).orElseThrow(()-> new UsernameNotFoundException("UserName Not Found"));

        if(user != null)
        {
            if(user.getRegistrationCompleted().equals("Y") && user.getIsMobileVerify().equals("Y")){
            response.setMessage("You are Already Authenticated Please Login ");
            response.setStatus(HttpStatus.OK);
            return ResponseEntity.ok(response);
            }
        }

        if(user == null){
            response.setMessage("Invaid User");
            return ResponseEntity.badRequest().body(response);
        }
        if(verifyMobileOtpPayload.getMobileOtp().equals(user.getMobileOtp()))
        {
            user.setIsMobileVerify("Y");
            this.userRepository.save(user);

            //Set Response Message
            response.setMessage("Verify OTP Success");
            response.setStatus(HttpStatus.OK);
            logger.info("OTP Verified Success");
            return ResponseEntity.ok(response);
        }else{
            //Set Response Message

            response.setMessage("OTP Not Verified ");
            response.setStatus(HttpStatus.BAD_REQUEST);
            logger.error("OTP not Verified" , response);
            return ResponseEntity.badRequest().body(response);
        }
    }
    @Override
    public ResponseEntity<?> customerSignUpCompleted(FreshSignUpPayload freshSignUpPayload) {
                User user = userRepository.findByUsername(freshSignUpPayload.getUsername()).
                orElseThrow(()-> new RuntimeException("User Not Fount"));

        if ( (user.getRegistrationCompleted().equals("N") ||  user.getRegistrationCompleted().isEmpty()
                || user.getRegistrationCompleted() == null)
                && user.getIsMobileVerify().equals("Y")) {

            user.setRegistrationCompleted("Y");
            user.setPassword(encoder.encode(freshSignUpPayload.getPassword()));

            //Set Project Role
            user.setProjectRole(ERole.ROLE_CUSTOMER.toString());

            userRepository.save(user);
            logger.info("Registration Completed Fully");

            return ResponseEntity.ok(new MessageResponse("Registration Completed Fully",HttpStatus.OK));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("Something Went Wrong",HttpStatus.BAD_REQUEST));
        }
    }


}