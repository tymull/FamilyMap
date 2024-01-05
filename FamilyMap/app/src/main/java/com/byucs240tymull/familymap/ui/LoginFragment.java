package com.byucs240tymull.familymap.ui;

import android.content.Context;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;
import com.byucs240tymull.familymap.tasks.LoginTask;
import com.byucs240tymull.familymap.tasks.RegisterTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Requests.LoginRequest;
import Requests.RegisterRequest;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {
    private static final String LOG_TAG = "LoginFragment";
    private static final String LOGIN_SUCCESS = "LoginSuccess"; //***NOT SURE WHAT TO DO HERE***
    private static final String REGISTER_SUCCESS = "RegisterSuccess";
    private static final String USER_FIRST_NAME = "UserFirstName";
    private static final String USER_LAST_NAME ="UserLastName";


    private EditText serverHostEditText;
    private EditText serverPortEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private RadioGroup genderRadioGroup;
    private Button signInButton;
    private Button registerButton;
    private boolean genderSelected = false;
    private boolean registerTextReady = false;

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //trim makes it so empty spaces don't count as valid
            String serverHostInput = serverHostEditText.getText().toString().trim();
            String serverPortInput = serverPortEditText.getText().toString().trim();
            String usernameInput = usernameEditText.getText().toString().trim();
            String passwordInput = passwordEditText.getText().toString().trim();
            String firstNameInput = firstNameEditText.getText().toString().trim();
            String lastNameInput = lastNameEditText.getText().toString().trim();
            String emailInput = emailEditText.getText().toString().trim();
            boolean signInReady;
            boolean registerReady;

            //check if necessary fields are filled to enable sign in button
            if (!serverHostInput.isEmpty() && !serverPortInput.isEmpty() && !usernameInput.isEmpty()
                    && !passwordInput.isEmpty()) {
                signInReady = true;
                //now check to see if all fields are filled to enable register button
                if (!firstNameInput.isEmpty() && !lastNameInput.isEmpty() && !emailInput.isEmpty()) {
                    registerTextReady = true;
                    //have to set this separate in the case of radio button being pushed after
                    //these checks have been made so don't have to recheck each piece when that happens
                    registerReady = genderSelected;
                }
                else {
                    registerReady = false;
                    registerTextReady = false;
                }
            }
            else {
                signInReady = false;
                registerReady = false;
                registerTextReady = false;
            }

            signInButton.setEnabled(signInReady);
            registerButton.setEnabled(registerReady);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) {this.listener = listener;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //link variables to their views in fragment xml
        serverHostEditText = view.findViewById(R.id.serverHostEditText);
        serverPortEditText = view.findViewById(R.id.serverPortEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        signInButton = view.findViewById(R.id.signInButton);
        registerButton = view.findViewById(R.id.registerButton);

        //listeners for text fields to enable or disable sign in and register buttons
        serverHostEditText.addTextChangedListener(loginTextWatcher);
        serverPortEditText.addTextChangedListener(loginTextWatcher);
        usernameEditText.addTextChangedListener(loginTextWatcher);
        passwordEditText.addTextChangedListener(loginTextWatcher);
        firstNameEditText.addTextChangedListener(loginTextWatcher);
        lastNameEditText.addTextChangedListener(loginTextWatcher);
        emailEditText.addTextChangedListener(loginTextWatcher);

        //acknowledge that a gender has been selected from radio group
        genderRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                genderSelected = true;
                //if the text is already good to go
                if (registerTextReady) {
                    registerButton.setEnabled(true);
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear cache before new login attempt ***maybe bad idea?***
                DataCache cache = DataCache.getInstance();
                cache.clearCache();

                //handler will process messages from task and update ui thread
                Handler uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        // this is where I pull data from bundle to make toast for login
                        boolean success = bundle.getBoolean(LOGIN_SUCCESS, false);
                        if (success) {
                            String firstName = bundle.getString(USER_FIRST_NAME, "Error");
                            String lastName = bundle.getString(USER_LAST_NAME, "Error");
                            String toastString = getString(R.string.user_first_name, firstName) + " " +
                                    getString(R.string.user_last_name, lastName);
                            //toast for Login assignment
                            Toast.makeText(getContext(), toastString, Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.notifyDone();
                            }
                        }
                        else {
                            //might want to use getApplication.getApplicationContext()
                            Toast.makeText(getContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                //create and execute login task on a separate thread
                LoginTask task = new LoginTask(uiThreadMessageHandler, getLoginRequest(),
                        serverHostEditText.getText().toString(), serverPortEditText.getText().toString());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear cache before new login attempt ***maybe bad idea?***
                DataCache cache = DataCache.getInstance();
                cache.clearCache();

                //handler will process messages from task and update ui thread
                Handler uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        // this is where I pull data from bundle to make toast for register
                        boolean success = bundle.getBoolean(REGISTER_SUCCESS, false);
                        if (success) {
                            String firstName = bundle.getString(USER_FIRST_NAME, "Error");
                            String lastName = bundle.getString(USER_LAST_NAME, "Error");
                            String toastString = getString(R.string.user_first_name, firstName) + " " +
                                    getString(R.string.user_last_name, lastName);
                            //toast for Login assignment
                            Toast.makeText(getContext(), toastString, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //might want to use getApplication.getApplicationContext()
                            Toast.makeText(getContext(), R.string.register_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                //create and execute login task on a separate thread
                RegisterTask task = new RegisterTask(uiThreadMessageHandler, getRegisterRequest(),
                        serverHostEditText.getText().toString(), serverPortEditText.getText().toString());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        return view;
    }

    //will make sure the necessary parameters are not null before these next two methods are called
    //uses provided information to create and return a login request
    public LoginRequest getLoginRequest() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        LoginRequest request = new LoginRequest(username, password);
        return request;
    }

    //uses provided information to create and return a register request
    public RegisterRequest getRegisterRequest() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String gender;
        // get selected radio button from radioGroup
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        RadioButton genderRadioButton = genderRadioGroup.findViewById(selectedId);
        if (genderRadioButton.getText().toString().equals("Male")) {
            gender = "m";
        }
        // will need to make sure one or the other is selected
        else {
            gender = "f";
        }

        RegisterRequest request = new RegisterRequest(username, password, email, firstName, lastName, gender);
        return request;
    }

    // The following callbacks are included to show the order of fragment callbacks in the log.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(LOG_TAG, "in onAttach(Context)");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "in onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "in onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "in onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "in onStop()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(LOG_TAG, "in onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "in onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOG_TAG, "in onDetach()");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "in onSaveInstanceState(Bundle)");
    }
}