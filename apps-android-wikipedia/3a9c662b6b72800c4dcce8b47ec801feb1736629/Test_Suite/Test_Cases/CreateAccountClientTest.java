package org.wikipedia.createaccount;

import android.support.annotation.NonNull;

import com.google.gson.stream.MalformedJsonException;

import org.junit.Test;
import org.wikipedia.dataclient.mwapi.MwException;
import org.wikipedia.dataclient.okhttp.HttpStatusException;
import org.wikipedia.test.MockWebServerTest;

import retrofit2.Call;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CreateAccountClientTest extends MockWebServerTest {
    @NonNull private CreateAccountClient subject = new CreateAccountClient();

    @Test public void testRequestSuccess() throws Throwable {
        enqueueFromFile("create_account_success.json");

        CreateAccountClient.Callback cb = mock(CreateAccountClient.Callback.class);
        Call<CreateAccountResponse> call = request(cb);

        server().takeRequest();
        assertCallbackSuccess(call, cb);
    }

    @Test public void testRequestFailure() throws Throwable {
        enqueueFromFile("create_account_failure.json");

        CreateAccountClient.Callback cb = mock(CreateAccountClient.Callback.class);
        Call<CreateAccountResponse> call = request(cb);

        server().takeRequest();
        assertCallbackFailure(call, cb, CreateAccountException.class);
    }

    @Test public void testRequestResponseApiError() throws Throwable {
        enqueueFromFile("api_error.json");

        CreateAccountClient.Callback cb = mock(CreateAccountClient.Callback.class);
        Call<CreateAccountResponse> call = request(cb);

        server().takeRequest();
        assertCallbackFailure(call, cb, MwException.class);
    }

    @Test public void testRequestResponse404() throws Throwable {
        enqueue404();

        CreateAccountClient.Callback cb = mock(CreateAccountClient.Callback.class);
        Call<CreateAccountResponse> call = request(cb);

        server().takeRequest();
        assertCallbackFailure(call, cb, HttpStatusException.class);
    }

    @Test public void testRequestResponseMalformed() throws Throwable {
        server().enqueue("┏━┓ ︵  /(^.^/)");

        CreateAccountClient.Callback cb = mock(CreateAccountClient.Callback.class);
        Call<CreateAccountResponse> call = request(cb);

        server().takeRequest();
        assertCallbackFailure(call, cb, MalformedJsonException.class);
    }

    private void assertCallbackSuccess(@NonNull Call<CreateAccountResponse> call,
                                       @NonNull CreateAccountClient.Callback cb) {
        verify(cb).success(eq(call), any(CreateAccountSuccessResult.class));
        //noinspection unchecked
        verify(cb, never()).failure(any(Call.class), any(Throwable.class));
    }

    private void assertCallbackFailure(@NonNull Call<CreateAccountResponse> call,
                                       @NonNull CreateAccountClient.Callback cb,
                                       @NonNull Class<? extends Throwable> throwable) {
        //noinspection unchecked
        verify(cb, never()).success(any(Call.class), any(CreateAccountSuccessResult.class));
        verify(cb).failure(eq(call), isA(throwable));
    }

    private Call<CreateAccountResponse> request(@NonNull CreateAccountClient.Callback cb) {
        return subject.request(service(CreateAccountClient.Service.class), "user", "pass", "pass",
                "token", "email", "11235813", "fibonacci", cb);
    }
}
