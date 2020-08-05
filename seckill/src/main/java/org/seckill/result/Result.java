package org.seckill.result;

public class Result<T> {
    private int code;

    private String msg;

    private T data;

    public Result(String msg, T data) {
        this.msg = msg;
        this.data = data;
    }

    public Result(T data) {
        this.data = data;
    }

    private Result(CodeMsg codeMsg){
        if (codeMsg != null){
            this.code = codeMsg.getCode();
            this.msg = codeMsg.getMsg();
        }
    }

    public static <T>Result<T> success(T data){
        return new Result<T>(data);
    }

    public static <T>Result<T> error(CodeMsg codeMsg){
        return new Result<>(codeMsg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
