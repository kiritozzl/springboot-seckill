package org.seckill.exception;

import org.seckill.result.CodeMsg;
import org.seckill.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();
        if (e instanceof GlobalException){
            GlobalException ge = (GlobalException)e;
            return Result.error(ge.getCodeMsg());
        }else if (e instanceof BindException){
            BindException be = (BindException)e;
            List<ObjectError> allErrors = be.getAllErrors();
            ObjectError objectError = allErrors.get(0);
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(objectError.getDefaultMessage()));
        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
