// -*- @author aeren_pozitif  -*- //
package dergi.degisim.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.r0adkll.slidr.Slidr;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.util.Util;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText pswd;
    private Button login;
    private Button register;
    private GoogleSignInButton glogin;

    private GoogleApiClient apiClient;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private final static int RC_SIGN_IN = 1302;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Slidr.attach(this);

        email = findViewById(R.id.email_input);
        pswd = findViewById(R.id.password_input);
        login = findViewById(R.id.login_button);
        register = findViewById(R.id.register_btn);
        glogin = findViewById(R.id.signInButton);

        login.setOnClickListener(v -> login());
        register.setOnClickListener(v -> register());
        glogin.setOnClickListener(v -> signIn());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
        requestIdToken("1040413724850-0qvare5jk9qpv0vupqjmumkm5evclnrf.apps.googleusercontent.com").requestEmail().build();

        apiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, connectionResult -> Toast.makeText(getApplicationContext(), "Bağlantı başarısız oldu", Toast.LENGTH_LONG).show()).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void login() {
        String em = email.getText().toString();
        String pd = pswd.getText().toString();

        if (Util.checkLoggedIn())
            auth.signOut();

        if (em.isEmpty() || pd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Alanları doldurun", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(em, pd).addOnSuccessListener(authResult -> {
            Toast.makeText(getApplicationContext(), "Giriş yapıldı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Parola veya e-posta doğru değil", Toast.LENGTH_LONG).show());
    }

    private void register() {
        String em = email.getText().toString();
        String pd = pswd.getText().toString();

        if (Util.checkLoggedIn())
            auth.signOut();

        if (em.isEmpty() || pd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Alanları doldurun", Toast.LENGTH_LONG).show();
            return;
        } else if (!em.contains("@")) {
            Toast.makeText(getApplicationContext(), "Geçerli bir e-posta girin", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(em, pd).addOnSuccessListener(authResult -> {
            Toast.makeText(getApplicationContext(), "Başarılı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference("users");
            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("markeds").setValue("empty");

            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Bir şeyler yanlış oldu, tüh :(", Toast.LENGTH_LONG).show());
    }

    private void signIn() {
        if (auth.getCurrentUser() != null)
            if (auth.getCurrentUser().isAnonymous())
                auth.signOut();

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, RC_SIGN_IN);
        Log.d("AUTH", "Intent started");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AUTH", "Request code: " + requestCode);
        Toast.makeText(getApplicationContext(), "RCODE: " + requestCode, Toast.LENGTH_SHORT).show();

        if (requestCode == RC_SIGN_IN) {
            Toast.makeText(getApplicationContext(), "RC_CODE is true " + requestCode, Toast.LENGTH_SHORT).show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Failed to authenticate, error: "  + e ,Toast.LENGTH_SHORT).show();
                Log.w("AUTH", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Toast.makeText(getApplicationContext(), "Authenticating ", Toast.LENGTH_SHORT).show();
        Log.d("AUTH", "authenticating..");

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).
        addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Başarılı @ auth", Toast.LENGTH_SHORT).show();
                FirebaseUser user = auth.getCurrentUser();


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("users");

                // Check if user is signing in first time
                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                if (isNew)
                    ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("markeds").setValue("empty");

                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Giriş yapılamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
