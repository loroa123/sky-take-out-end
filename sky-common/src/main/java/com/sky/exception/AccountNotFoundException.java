package com.sky.exception;

/**
 * 账号不存在异常
 */
//继承BaseException
public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException() {
    }

    public AccountNotFoundException(String msg) {
        super(msg);
    }

}
