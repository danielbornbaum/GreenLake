package util;

public class HTTPStatusCodes
{
    public static interface Codes
    {
        public int getCode();
    }

    public static enum SUCCESS_CODES implements Codes
    {
        OK(200),
        CREATED(201),
        NO_CONTENT(204),
        RESET_CONTENT(205),
        PARITAL_CONTENT(206);

        private final int code;

        SUCCESS_CODES(int code)
        {
            this.code = code;
        }

        @Override
        public int getCode()
        {
            return code;
        }
    }

    public static enum CLIENT_ISSUES implements Codes
    {
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        CONFLICT(409),
        GONE(410),
        UNSUPPORTED_MEDIA_TYPE(415);

        private final int code;

        CLIENT_ISSUES(int code)
        {
            this.code = code;
        }

        @Override
        public int getCode()
        {
            return code;
        }
    }

    public static enum SERVER_ISSUES implements Codes
    {
        INTERNAL_SERVER_ERROR(500),
        NOT_IMPLEMENTED(501);

        private final int code;

        SERVER_ISSUES(int code)
        {
            this.code = code;
        }

        @Override
        public int getCode()
        {
            return code;
        }
    }
}
