
package org.example.chat.protocal.s;

public class PLoginInfoResponse {
    protected int code = 0;

    protected long firstLoginTime = 0;

    public PLoginInfoResponse(int code, long firstLoginTime) {
        this.code = code;
        this.firstLoginTime = firstLoginTime;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getFirstLoginTime() {
        return firstLoginTime;
    }

    public void setFirstLoginTime(long firstLoginTime) {
        this.firstLoginTime = firstLoginTime;
    }
}
