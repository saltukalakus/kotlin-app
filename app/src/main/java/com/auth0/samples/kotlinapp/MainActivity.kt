package com.auth0.samples.kotlinapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.lock.AuthenticationCallback
import com.auth0.android.lock.Lock
import com.auth0.android.lock.LockCallback
import com.auth0.android.lock.utils.LockException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object Extras {
        const val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    }

    private var auth0Lock: Lock? = null

    private val authCallback: LockCallback = object : AuthenticationCallback() {
        // Authenticated
        override fun onAuthentication(credentials: Credentials) {
            // save Auth0 credentials to app preferences
            MyApplication.getCredentialsManager().saveCredentials(credentials)

            // flush error message for success login
            errorTv.text = ""

            // launch Home activity with list of customers
            openHomeScreen()
        }

        //User pressed back, do nothing
        override fun onCanceled() {}

        // Exception occurred
        override fun onError(error: LockException) {
            errorTv.text = getString(R.string.authenticationError)
            error.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(EXTRA_CLEAR_CREDENTIALS, false)) {
            logout()
        }

        setContentView(R.layout.activity_main)

        setUpListeners()
        credentialsCheck()
    }

    private fun credentialsCheck() {
        // this is a prototype, you should not use global variables like here.
        // an extra can trigger a special if case in an activity.
        if (MyApplication.getCredentialsManager().hasValidCredentials()) openHomeScreen()
        // if credentials is not valid, just leave the user on that screen
    }

    // https://auth0.com/docs/libraries/lock-android/v2
    private fun setupAuth0Lock() {
        auth0Lock = Lock.newBuilder(MyApplication.getAuth0(), authCallback)
            .withScheme(getString(R.string.auth0CallbackScheme))
            .withAudience(getString(R.string.auth0Audience))
            .withScope(getString(R.string.auth0Scope))
            .closable(true)
            .build(this)
    }

    // https://auth0.com/docs/quickstart/native/android/00-login
    private fun login() {
        WebAuthProvider.login(MyApplication.getAuth0())
            .withScheme(getString(R.string.auth0CallbackScheme))
            // the auth audience API may need to include https://
            .withAudience(getString(R.string.auth0Audience))
            .start(this, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    runOnUiThread { dialog.show() }
                }

                override fun onFailure(exception: AuthenticationException) {
                    //throw exception;
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity, "Something went wrong!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onSuccess(credentials: Credentials) {
                    CredentialsManager.saveCredentials(this@MainActivity, credentials)
                    this@MainActivity.openHomeScreen()
                }
            })
    }

    private fun logout() {
        // we use the SecureCredentialsManager and we need to clear it
        MyApplication.getCredentialsManager().clearCredentials()
        // we must logout in the browser
        WebAuthProvider.logout(MyApplication.getAuth0())
            .withScheme(getString(R.string.auth0CallbackScheme))
            .start(this, object : VoidCallback {
                override fun onSuccess(payload: Void?) {}
                override fun onFailure(error: Auth0Exception) {
                    error.printStackTrace()
                }
            })
    }

    private fun setUpListeners() {
        loginBtn.setOnClickListener {
            // both native screen, and browser login were tested to work in an auth0 test account
            // one can login using a browser (universal login) that is more secured
            // or one can login in the native screen (which disables single sign-in feature).
            // note that the native screen, requires that you activate the password grant
            // in the auth0 account. It is slightly faster in the UI to use the native  screen.

            // using the lock for a native screen is discouraged, and it will disable
            // single the sign in feature. One should login via universal login in a browser.
            // for a lock, you need to change the auth0 Application settings and add
            // the password grant permission in the table at the bottom if you click
            // advanced settings
            // ----------------> uncomment this and comment login to use the native screen login:
            //setupAuth0Lock()
            //startActivity(auth0Lock?.newIntent(this))

            // Problem with universal login that is bugged in this project:
            // It can happen that auth0 gets stuck via universal login for a certain application
            // and this while the same code works with another application
            // the code is very sensitive to how the audience, domain, etc, look like.

            // the login in a browser is inspired from the official auth0 pure java doc:
            // https://auth0.com/docs/quickstart/native/android/00-login
            // and from an old kotlin tutorial (with old packages):
            // https://auth0.com/blog/authenticating-android-apps-developed-with-kotlin/
            login()

            // Important redirect issues in old chrome versions (69) when logging out:
            /*
            update: as said at the end, only old chrome versions have this problem.
            And teh issue is not critical. There is no problem on iOS since they always use the
            native browser for in-app browser, and not chrome even if chrome is installed and teh default.

            This issue was not reported to auth0 support because it was fixed in chrome.
            The issue can be reproduce with the present sample code.
            With emulator Nexus6 API 28, the custom one not the recommended one, in Hardware graphics mode,
            with custom of RAM of 3GB. Uninstalling the app does not change things.

            With universal login in a custom tabs, or in a widget, we get sometimes (not every time),
            when logging out, an adb info log "Navigation is blocked" from the browser, and the
            native app gets stuck on a white screen.

            02-04 11:42:20.637  4117  4117 I chromium: [INFO:CONSOLE(0)] "Navigation is blocked: https://CHANGED_DOMAIN/android/com.auth0.samples.kotlinapp/callback", source:  (0)

            Info for how to enable single sin-in later with universal login:
            We wanted to use universal login and not native screens.
            Only universal login allows single sign-in functionality.
            However, we kept the native screens, because universal login would take time.
            It requires to change the SDK due to redirect issues.

            It is mandatory to logout in the browser to clear the cookies.
            We get white screens sometimes when logging out. Google Chrome blocks the logout redirect.
            They do not allow logins in an in-app browser, only in a device browser. To fix this,
            the google docs say we should use a new SDK (like AppAuth-Android).

            One can also patch the Auth0 SDK to open in a new browser.
            However, 2 different patches did not solve the issue, although the first patch
            changes the frequency of the bug to 1/50 instead of 1/5.

            patch 1:
            AuthorizationActivity.java (unzip and rezip the auth0 archive with the changes and run in debug mode)

            private void launchAuthenticationIntent() {
                    Bundle extras = getIntent().getExtras();
                    Uri authorizeUri = extras.getParcelable(EXTRA_AUTHORIZE_URI);
                    if (!extras.getBoolean(EXTRA_USE_BROWSER, true)) {
                        Intent intent = new Intent(this, WebAuthActivity.class);
                        intent.setData(authorizeUri);
                        intent.putExtra(WebAuthActivity.CONNECTION_NAME_EXTRA, extras.getString(EXTRA_CONNECTION_NAME));
                        intent.putExtra(WebAuthActivity.FULLSCREEN_EXTRA, extras.getBoolean(EXTRA_USE_FULL_SCREEN));
                        //The request code value can be ignored
                        startActivityForResult(intent, 33);
                        return;
                    }

            patch 2:
            AuthorizationActivity.java (unzip and rezip the auth0 archive with the changes and run in debug mode)
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.google.android.browser","com.google.android.browser.BrowserActivity");
            intent.setComponent(comp);
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.setData(authorizeUri);
            startActivity(intent);
            return;

            The first link claim to have a fix for it using the AuthApp-Android SDK demo.
            https://android.develop-bugs.com/article/12861158/%E2%80%9CNavigation+is+blocked%E2%80%9D+when+redirecting+from+Chrome+Custom+Tab+to+Android+app
            https://github.com/openid/AppAuth-Android
            https://developers.googleblog.com/2016/08/modernizing-oauth-interactions-in-native-apps.html

            In fact the last link only says chrome will not support widgets, but custom tabs are still good.
            There are other issue bugs on the chromium issue trackers about this Navigation is blocked.
            https://bugs.chromium.org/p/chromium/issues/detail?id=738724
            They say it is fixed in the latest Chrome.

            However, users and emulators maye use old Chrome version.
            This is issue is not 100% critical. If a user freezes on the logout screen,
            he can press the device's back button.

            Interesting recovery solution:
            It is possible to use a timer to force the logout browser activity to end and have a new login activity
            Then the login will get an exception, so the onFailure must run auth0's logout again. And the auth0 activity
            to log out will end without exception. A second timer on the login can force to login again.
            A boolean must be set to true when the logout freezes, and when the next login gets an exception,
            we only do a logout/login if the boolean is true, and we set it off.
            The fix was not perfect since there is uncertainty and between transitions, non relevant screens are shown
            This solution is not given here since the fix is not so important.
            */
        }
    }

    private fun openHomeScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        auth0Lock?.onDestroy(this)
        auth0Lock = null
    }
}
