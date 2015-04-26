package com.orange.game.model.manager.group.exception;

import com.orange.game.constants.ErrorCode;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-2
 * Time: 上午10:44
 * To change this template use File | Settings | File Templates.
 */
public class GroupException extends Exception {
    int errorCode = ErrorCode.ERROR_GROUP;

    private GroupException(int errorCode)
    {
        super();
        this.errorCode = errorCode;
    }

    public int getErrorCode(){
        return errorCode;
    }


    public final static class DuplicateGroupNameException extends GroupException{
        public DuplicateGroupNameException()
        {
             super(ErrorCode.ERROR_GROUP_DUPLICATE_NAME);
        }
    }

    public final static class MultipleJoinException extends GroupException {
        public MultipleJoinException(){
            super(ErrorCode.ERROR_GROUP_MULTIJOINED);
        }
    }
}
