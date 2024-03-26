//package com.ecom.fyp2023.CalendarAPI;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.ecom.fyp2023.R;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.Scope;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.api.services.calendar.CalendarScopes;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//
//public class GoogleCalendarAPI {
//
//    public static final int RC_SIGN_IN = 9001;
//
//    private Context context;
//    private GoogleSignInClient googleSignInClient;
//
//    public GoogleCalendarAPI(@NonNull Context context) {
//        this.context = context;
//
//        // Initialize the Google sign-in provider
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(context.getString(R.string.default_web_client_id))
//                .requestEmail()
//                .requestScopes(new Scope(CalendarScopes.CALENDAR))
//                .build();
//
//        googleSignInClient = GoogleSignIn.getClient(context, gso);
//
//        // Check if the user is already signed in
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
//
//        if (account == null) {
//            // Start the sign-in intent
//            signIn();
//        } else {
//            // Get the Google sign-in credential
//            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//            // Sign in the user with Firebase
//            firebaseAuthWithGoogle(credential);
//        }
//    }
//
//    private void signIn() {
//        // Start the sign-in intent
//        Intent signInIntent = googleSignInClient.getSignInIntent();
//        ((Activity) context).startActivityForResult(signInIntent, RC_SIGN_IN);
//        // Make sure to call this method from an Activity using startActivityForResult
//        // For example, if this is in a fragment, you can call getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    // Create this method and add the logic to sign in the user with Firebase
//    private void firebaseAuthWithGoogle(AuthCredential credential) {
//        // Get the Firebase auth instance
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        // Sign in the user with the credential
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success
//                            Log.d("TAG", "signInWithCredential:success");
//                            // Get the current user
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//                            // Do something with the user
//                        } else {
//                            // Sign in failure
//                            Log.w("TAG", "signInWithCredential:failure", task.getException());
//                            // Show an error message
//                        }
//                    }
//                });
//    }
//
//    public void handleSignInResult(Task<GoogleSignInAccount> task) {
//
//
//    }
//}
//
//
//    implementation ("com.google.api-client:google-api-client:2.2.0")
//    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.32.1")
//    implementation ("com.google.apis:google-api-services-calendar:v3-rev20240111-2.0.0")