package com.lenovo.vro.pricing.configuration;

public class CodeConfig {
    public static final String ROLE_PREFIX = "ROLE_";
    // token 有效期
    public static final long TOKEN_CONTINUE_TIME = 60 * 60 * 1000;

    public static final int LIST_NUMBER = 1000;
    public static final int LIST_DELETE_NUMBER = 10000;

    public static final String DELETE_VALID = "1";
    public static final String DELETE_INVALID = "0";

    public static final String OPERATION_SUCCESS = "1";
    public static final String OPERATION_FAILED = "0";
    public static final String OPERATION_NOTEXECUTE = "2";

    public static final String OPERATION_SUCCESS_MESSAGE = "上传成功!";
    public static final String OPERATION_FINAL_MESSAGE = "上传失败，请检查Log!";
}
