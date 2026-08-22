package com.todaktodot.TDTD.domain.vote.exception;

import com.todaktodot.TDTD.global.exception.ErrorCode;
import com.todaktodot.TDTD.global.exception.TdtdException;

public class VoteException extends TdtdException {
    public VoteException(ErrorCode errorCode) {
        super(errorCode);
    }
}
