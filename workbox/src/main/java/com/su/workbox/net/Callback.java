package com.su.workbox.net;

import java.io.IOException;

/**
 * Created by su on 17-4-11.
 */

public interface Callback<T> {

    void onFailure(IOException exception);

    void onCancel();

    void onError(NetResponse<T> response) throws IOException;

    void onResponse(NetResponse<T> response) throws IOException;
}
