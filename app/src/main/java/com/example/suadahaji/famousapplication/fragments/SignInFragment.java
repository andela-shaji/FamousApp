package com.example.suadahaji.famousapplication.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.suadahaji.famousapplication.R;
import com.example.suadahaji.famousapplication.utilities.DisplayUtility;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class SignInFragment extends BaseFragment{
    /*@BindView(R.id.toolbar)
    Toolbar toolbar;*/
    @BindView(R.id.email_til)
    TextInputLayout emailInputLayout;
    @BindView(R.id.password_til)
    TextInputLayout passwordInputLayout;
    @BindView(R.id.password_et)
    EditText passwordEditText;
    @BindView(R.id.email_et)
    EditText emailEditText;
    /*@BindView(R.id.sign_in_ll)
    LinearLayout signInLinearLayout;*/
    @BindView(R.id.sign_in_btn)
    Button signInButton;

    private Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
    private Matcher matcher;

    Unbinder unbinder;


    @OnClick(R.id.sign_in_btn)
    public void onSignInButtonClicked(View view) {
        DisplayUtility.hideKeyboard(getContext(), view);

        Snackbar.make(getActivity().findViewById(android.R.id.content),
                getString(R.string.sign_in_message),
                Snackbar.LENGTH_LONG)
                .show();
    }

    public SignInFragment() {

    }

    public static SignInFragment newInstance(Bundle extras) {
        SignInFragment fragment = new SignInFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_login, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.sign_in));
        }
*/
        Observable<CharSequence> emailChangeObservable = RxTextView.textChanges(emailEditText);
        Observable<CharSequence> passwordChangeObservable = RxTextView.textChanges(passwordEditText);

        // Checks for validity of the email input field

        Subscription emailSubscription = emailChangeObservable
                .doOnNext(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        hideEmailError();
                    }
                })
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return !TextUtils.isEmpty(charSequence);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        boolean isEmailValid = validateEmail(charSequence.toString());
                        if (!isEmailValid) {
                            showEmailError();
                        } else {
                            hideEmailError();
                        }

                    }
                });

        compositeSubscription.add(emailSubscription);

        Subscription passwordSubscription = passwordChangeObservable
                .doOnNext(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        hidePasswordError();
                    }
                })
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        return !TextUtils.isEmpty(charSequence);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        boolean isPasswordValid = validatePassword(charSequence.toString());
                        if (!isPasswordValid) {
                            showPasswordError();
                        } else {
                            hidePasswordError();
                        }
                    }
                });
        compositeSubscription.add(passwordSubscription);

        // Checks for validity of all input fields

        Subscription signInFieldsSubscription = Observable.combineLatest(emailChangeObservable, passwordChangeObservable, new Func2<CharSequence, CharSequence, Boolean>() {

            @Override
            public Boolean call(CharSequence email, CharSequence password) {
                boolean isEmailValid = validateEmail(email.toString());
                boolean isPasswordValid = validatePassword(password.toString());

                return isEmailValid && isPasswordValid;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean validFields) {
                        if (validFields) {
                            enableSignIn();
                        } else {
                            disableSignIn();
                        }
                    }
                });

        compositeSubscription.add(signInFieldsSubscription);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // region Helper Methods
    private void enableError(TextInputLayout textInputLayout) {
        if (textInputLayout.getChildCount() == 2)
            textInputLayout.getChildAt(1).setVisibility(View.VISIBLE);
    }

    private void disableError(TextInputLayout textInputLayout) {
        if (textInputLayout.getChildCount() == 2)
            textInputLayout.getChildAt(1).setVisibility(View.GONE);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email))
            return false;

        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        return password.length() > 5;
    }

    private void showEmailError(){
        enableError(emailInputLayout);
        // emailInputLayout.setErrorEnabled(true);
        emailInputLayout.setError(getString(R.string.invalid_email));
    }

    private void hideEmailError(){
        disableError(emailInputLayout);
        // emailInputLayout.setErrorEnabled(false);
        emailInputLayout.setError(null);
    }

    private void showPasswordError(){
        enableError(passwordInputLayout);
        // passwordInputLayout.setErrorEnabled(true);
        passwordInputLayout.setError(getString(R.string.invalid_password));
    }

    private void hidePasswordError(){
        disableError(passwordInputLayout);
        // passwordInputLayout.setErrorEnabled(false);
        passwordInputLayout.setError(null);
    }

    private void enableSignIn(){
        signInButton.setEnabled(true);
        signInButton.setBackgroundColor(getResources().getColor(R.color.redButton));
    }

    private void disableSignIn(){
        signInButton.setEnabled(false);
        signInButton.setBackgroundColor(getResources().getColor(R.color.pink_200));
    }

}
