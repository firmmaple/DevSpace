package org.jeffrey.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResVo<T> implements Serializable {
    @Schema(description = "返回结果说明", required = true)
    private Status status;

    @Schema(description = "返回的实体结果", required = true)
    private T result;

    private static final String OK_DEFAULT_RESULT = "ok";

    public ResVo() {
    }

    public ResVo(Status status) {
        this.status = status;
    }

    public ResVo(T result) {
        status = Status.newStatus(StatusEnum.SUCCESS);
        this.result = result;
    }

    public ResVo(Status status, T result) {
        this.status = status;
        this.result = result;
    }

    public static <T> ResVo<T> ok(T result) {
        return new ResVo<>(result);
    }

    public static ResVo<String> ok() {
        return ok(OK_DEFAULT_RESULT);
    }

    public static <T> ResVo<T> fail(StatusEnum status, Object... args) {
        return new ResVo<>(Status.newStatus(status, args));
    }

    public static <T> ResVo<T> fail(Status status) {
        return new ResVo<>(status);
    }
}