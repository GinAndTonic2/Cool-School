package com.coolSchool.CoolSchool.exceptions.blog;

import com.coolSchool.CoolSchool.exceptions.common.ApiException;
import org.springframework.http.HttpStatus;

public class BlogNotEnabledException extends ApiException {
    public BlogNotEnabledException() {
            super("The blog will be visible after admin approval!", HttpStatus.BAD_REQUEST);
    }
}
