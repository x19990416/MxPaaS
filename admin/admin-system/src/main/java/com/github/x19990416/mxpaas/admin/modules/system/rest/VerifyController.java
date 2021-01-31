/**
 * create by Guo Limin on 2021/1/31.
 */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.utils.enums.CodeBiEnum;
import com.github.x19990416.mxpaas.admin.common.utils.enums.CodeEnum;
import com.github.x19990416.mxpaas.admin.modules.system.service.VerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
@Tag(name = "系统：验证码管理")
public class VerifyController {

    private final VerifyService verificationCodeService;
   // private final EmailService emailService;

    @PostMapping(value = "/resetEmail")
    @Operation(method ="重置邮箱，发送验证码")
    public ResponseEntity<Object> resetEmail(@RequestParam String email){
        /*EmailVo emailVo = verificationCodeService.sendEmail(email, CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey());
        emailService.send(emailVo,emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
        */
        return null;
    }

    @PostMapping(value = "/email/resetPass")
    @Operation(method ="重置密码，发送验证码")
    public ResponseEntity<Object> resetPass(@RequestParam String email){
      //  EmailVo emailVo = verificationCodeService.sendEmail(email, CodeEnum.EMAIL_RESET_PWD_CODE.getKey());
       // emailService.send(emailVo,emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/validated")
    @Operation(method = "验证码验证")
    public ResponseEntity<Object> validated(@RequestParam String email, @RequestParam String code, @RequestParam Integer codeBi){
        CodeBiEnum biEnum = CodeBiEnum.find(codeBi);
        switch (Objects.requireNonNull(biEnum)){
            case ONE:
                verificationCodeService.validated(CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + email ,code);
                break;
            case TWO:
                verificationCodeService.validated(CodeEnum.EMAIL_RESET_PWD_CODE.getKey() + email ,code);
                break;
            default:
                break;
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
