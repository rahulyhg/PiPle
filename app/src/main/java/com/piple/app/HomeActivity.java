package com.piple.app;



import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.piple.res.Contact;
import com.piple.res.Universe;
import com.piple.res.UniverseAdapter;
import com.piple.res.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static android.R.attr.id;


/**
 * Class HomeActivity
 *      extends AppCompatActivity
 *      implements View.OnClickListener
 *      implements GoogleApiClient.OnConnectionFailedListener
 *
 * First activity of the app.
 * If the user is not logged in, redirects on LoginActivity.
 * Displays the home page (containing the different universes) of the user.
 */
public class HomeActivity
        extends
            AppCompatActivity
        implements
            View.OnClickListener,
            GoogleApiClient.OnConnectionFailedListener
{



    /// RESOURCES ///

    private static final String TAG = "HomeActivity";
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference myRefUniverse;
    private DatabaseReference myRefUser;
    private FirebaseDatabase database;
    private User yourself;
    private boolean UserhasUniverses;
    private String m_Universename = "";

    //LAYOUT
    public ListView mlistview;
    private List<Universe> Universes;

    /// METHODS ///

    /**
     * Method onCreate
     *      overrides method from AppCompatActivity
     *
     * Implements the behavior of the activity when it is created.
     *
     * @param savedInstanceState saved state from the program
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().setTitle("PiPle - Universe");
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        setContentView(R.layout.home);
        findViewById(R.id.buttonnewuniverse).setOnClickListener(this);

        //la listview dans laquelle on set un onclick listener pour chaques item
        mlistview = (ListView) findViewById(R.id.listname);
        database = FirebaseDatabase.getInstance();
        Universes= new ArrayList<>();

        myRefUniverse = database.getReference("Universe");
        myRefUser = database.getReference("User");

        //Set preferences and defaults
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        //Initialize Firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        //Check if user is identified
        if (mFirebaseUser == null) {
            System.out.println("\n user : "+mFirebaseUser);
            //If no, launch LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            System.out.println("\n user : "+mFirebaseUser);
            return;
        }
            //If yes, get his email and welcome him
            mUsername = mFirebaseUser.getEmail();
            Toast.makeText(HomeActivity.this, "Hey there, " + mUsername, Toast.LENGTH_SHORT).show();
            yourself = new User(mFirebaseUser.getUid(), mFirebaseUser.getEmail());





    }



    /**
     * Method onStart
     *      overrides method from AppCompatActivity
     *
     * Implements the behavior of the activity when it is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        //TODO : get user's contacts ( later)



        myRefUniverse.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> universeMap = (HashMap<String, Object>) dataSnapshot.getValue();
                System.out.println("value gotten");
                Universe aUniverse = new Universe();
                aUniverse = aUniverse.toUniverse(universeMap);
                ArrayList UserList = aUniverse.getUniverseUserList();
                ListIterator iterator = UserList.listIterator();

                while (iterator.hasNext()) {

                    Contact cont = (Contact) iterator.next();

                    if (cont.getId().equals(yourself.getId())) {
                        System.out.println(yourself.getId());

                        Universes.add(aUniverse);
                        UserhasUniverses=true;




                    }

                }MajLayout();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void MajLayout(){
        if (UserhasUniverses) {
            createNewButton(Universes);
            mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    //on prend l'univers à la position correspondante en esperant que ça soit la même
                    String currentUniverseId = Universes.get(position).getId();

                    //on envois l'info de l'univers selectioné à l'univers activity puis on l'appel.
                    Intent sendUniverse = new Intent(HomeActivity.this, UniverseActivity.class);
                    sendUniverse.putExtra("currentUniverse", currentUniverseId);
                    startActivity(sendUniverse);
                    finish();
                }
            });

        }
    }
    @Override
    public void onResume(){
        super.onResume();


    }
    @Override
    public void onStop(){
        super.onStop();

        // Remove post value event listener

    }



    /**
     * Method onConnectionFailed
     *      overrides method from GoogleApiClient.OnConnectionFailedListener
     *
     * Implements the behavior of the activity when the connection to Google APIs failed.
     *
     * @param connectionResult ???
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    public boolean createNewButton(List Universes){

        //on créer un adapter qui contiendra grosse modo touts les element de la liste univers
        UniverseAdapter adapter = new UniverseAdapter(HomeActivity.this, Universes);
        System.out.println("putted");
        mlistview.setAdapter(adapter);
        System.out.println("putted");

    return true; // c'était au cas ou
    }


    /**
     * Method onClick
     *      overrides method from View.OnClickListener
     *
     * Implements the behavior of the activity when the user clicks on it.
     *
     * @param v view touched
     */
    @Override
    public void onClick(View v)
    {
        int i = v.getId();
        if (i == R.id.buttonnewuniverse) {

            //on créer une boite de dialogue avvec de l'input dedans
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Universe name :");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            builder.setView(input);


            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    m_Universename = input.getText().toString();
                    Universe myuniverse = new Universe(new Contact(mFirebaseUser.getEmail(),mFirebaseUser.getUid()), m_Universename, m_Universename);

                    //c'est comme ça que l'on save de la data de la bonne façon
                    String key = myRefUniverse.push().getKey();
                    Map<String, Object> UniverseValues = myuniverse.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(key, UniverseValues);

                    //on peut en sauver de grandes quantité d'un coup avec plusieurs put
                    myRefUniverse.updateChildren(childUpdates);
                    Toast.makeText(HomeActivity.this, "newuniverse created.", Toast.LENGTH_SHORT).show();

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }


        }





}


