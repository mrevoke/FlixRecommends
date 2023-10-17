package com.example.flixrecommends;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    int adcounter=1;
    private final String TAG ="GADS";
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    EditText messageEditText;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    Intent data;
    TextToSpeech textToSpeech;
 private AdView adView;
    public String USER_AGENT = "(Android " + Build.VERSION.RELEASE + ") Chrome/110.0.5481.63 Mobile";

    private AppOpenAd mappOpenAd;
    private boolean isShowingAd= false;
    private InterstitialAd minterstitialAd;
private void  showOpenAd(){
    AdRequest adRequest=new AdRequest.Builder().build();
    AppOpenAd.load(this, "ca-app-pub-6428792234577919/5046666054", adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
        }

        @Override
        public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
            super.onAdLoaded(appOpenAd);
            mappOpenAd=appOpenAd;
         if(!isShowingAd)
            mappOpenAd.show(MainActivity.this);
         isShowingAd=true;
        }
    });
}
    @Override
    protected void onResume() {
        super.onResume();

        if(!isShowingAd)  { showOpenAd();}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView=findViewById(R.id.adView);

        bannerAds();

     AdRequest adRequest = new AdRequest.Builder().build();

           InterstitialAd.load(this,"ca-app-pub-6428792234577919/5043592784", adRequest,
             new InterstitialAdLoadCallback() {
           @Override
           public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
             // The mInterstitialAd reference will be null until
             // an ad is loaded.
             minterstitialAd = interstitialAd;
             Log.i(TAG, "onAdLoaded");
           }

           @Override
           public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
             // Handle the error
             Log.d(TAG, loadAdError.toString());
             minterstitialAd = null;
           }
         });



        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Please Wait...");


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(USER_AGENT);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);


        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("https://chat.openai.com");


        /*Alertnate code for  startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
          This code replaces deprecated startActivityForResult() method*/
        someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    data = result.getData();
                }
            }

        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });
// Find the Send button and EditText in your layout
        ImageButton sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        Button alreadyseenButton = findViewById(R.id.alreadySeenButton);
        Button Summary = findViewById(R.id.reeditButton);

        Summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message1= "Give summary of those films";// Concatenate the user's name and the message

                String stringBuilder = "(function() { var d = document.getElementsByTagName('textarea').length; document.getElementsByTagName('textarea')[d-1].value='" +
                        message1 +
                        "';document.querySelector('button.absolute').disabled = false;" +
                        "document.querySelector('button.absolute').click(); })();";
                webView.evaluateJavascript(stringBuilder,null);
//                webView.evaluateJavascript(stringBuilder, new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        // After sending the message, make the WebView visible
//                        webView.setVisibility(View.VISIBLE);
//                    }
//                });
                // Clear the EditText after sending the message
                if(adcounter%2!=0) {
                    if (minterstitialAd != null) {
                        minterstitialAd.show(MainActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");

                    }
                //    Toast.makeText(getBaseContext(), "Ad is Loading !!", Toast.LENGTH_SHORT).show();

                }   // TODO Auto-generated method stub

                messageEditText.setText(messageEditText.getText());
                // Simulate pressing the "A" key
                KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
                KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE);
                webView.requestFocus();
// Dispatch the key events to the currently focused view
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.dispatchKeyEvent(keyEventDown);
                    currentFocus.dispatchKeyEvent(keyEventUp);
                }

                webView.evaluateJavascript(stringBuilder,null);
//messageEditText.setText("");
            }
        });
        alreadyseenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message1= "i have already seen it";// Concatenate the user's name and the message

                String stringBuilder = "(function() { var d = document.getElementsByTagName('textarea').length; document.getElementsByTagName('textarea')[d-1].value='" +
                        message1 +
                        "';document.querySelector('button.absolute').disabled = false;" +
                        "document.querySelector('button.absolute').click(); })();";
                webView.evaluateJavascript(stringBuilder,null);
//                webView.evaluateJavascript(stringBuilder, new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        // After sending the message, make the WebView visible
//                        webView.setVisibility(View.VISIBLE);
//                    }
//                });
                // Clear the EditText after sending the message
                if(adcounter%2!=0) {
                    if (minterstitialAd != null) {
                        minterstitialAd.show(MainActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");

                    }
               //     Toast.makeText(getBaseContext(), "Ad is Loading !!", Toast.LENGTH_SHORT).show();

                }   // TODO Auto-generated method stub

                messageEditText.setText(messageEditText.getText());
                // Simulate pressing the "A" key
                KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
                KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE);
                webView.requestFocus();
// Dispatch the key events to the currently focused view
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.dispatchKeyEvent(keyEventDown);
                    currentFocus.dispatchKeyEvent(keyEventUp);
                }

                webView.evaluateJavascript(stringBuilder,null);
//messageEditText.setText("");
            }
        });



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
oeee();

            }
        });

        // Create an UtteranceProgressListener object to handle callbacks
        UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

                // Called when TTS starts speaking
            }

            @Override
            public void onDone(String utteranceId) {
                // Called when TTS finishes speaking
            }

            @Override
            public void onError(String utteranceId) {
                // Called when TTS encounters an error
            }
        };
        // Add the UtteranceProgressListener to the TextToSpeech object
        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                progressBar.setProgress(newProgress);
                progressDialog.show();
                if (newProgress == 100) {
                    progressDialog.dismiss();
                }

                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private void bannerAds() {
        adView.loadAd(new AdRequest.Builder().build());
  adView.setAdListener(new AdListener() {
      @Override
      public void onAdClicked() {
          Log.d(TAG, "onAdClicked: ");
            }

      @Override
      public void onAdClosed() {
          Log.d(TAG, "onAdClosed: ");  
      }

      @Override
      public void onAdImpression() {

          Log.d(TAG, "onAdImpression: "); 
      }

      @Override
      public void onAdLoaded() {
          Log.d(TAG, "onAdLoaded: ");
      }

      @Override
      public void onAdOpened() {
          Log.d(TAG, "onAdOpened: ");      }

      @Override
      public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
          Log.d(TAG, "onAdFailedToLoad: "+loadAdError.getMessage());      }
  });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();

        } else {
            closeApp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.exit) {
            closeApp();

        } else if (itemId == R.id.speak) {
            onSpeakerPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    private void onSpeakerPressed() {
        String allParagraph;

        try {

            allParagraph = "var paragraphs = document.getElementsByClassName('markdown');" +
                    "if (paragraphs.length){" +
                    "paragraphs = paragraphs[paragraphs.length - 1].childNodes;" +
                    "var combinedText='';" +
                    "for (var i = 0; i < paragraphs.length; i++) {" +
                    "combinedText+=paragraphs[i].innerText;" +
                    "}combinedText;" +
                    "}";

            webView.evaluateJavascript(allParagraph, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                    String utteranceId = UUID.randomUUID().toString();
                    textToSpeech.speak(value, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                }
            });

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onMicPressed() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");

        try {
            someActivityResultLauncher.launch(intent);
            webView.requestFocus();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void closeApp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Exit ?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            //    webView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.requestFocus();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data != null) {
                String userMessage = "enlist me 5 movies to watch next, without description, ifi like these movies:"; // User's name and a message
                String speechResult = data.getStringArrayListExtra("android.speech.extra.RESULTS").get(0);
                String message = userMessage + " " + speechResult+""; // Concatenate the user's name and the message

                String stringBuilder = "(function() { var d = document.getElementsByTagName('textarea').length; document.getElementsByTagName('textarea')[d-1].value='" +
                        message +
                        "';document.querySelector('button.absolute').disabled = false;" +
                        "document.querySelector('button.absolute').click(); })();";
                webView.evaluateJavascript(stringBuilder, null);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void oeee(){
        {
            String message = messageEditText.getText().toString();

            adcounter++;
            String userMessage = " give list of 5 movies i should watch next, without description , if i like these movies:"; // User's name and a message
            //  String speechResult = data.getStringArrayListExtra("android.speech.extra.RESULTS").get(0);
            String message1 = userMessage + " "+message +""; // Concatenate the user's name and the message

            String stringBuilder = "(function() { var d = document.getElementsByTagName('textarea').length; document.getElementsByTagName('textarea')[d-1].value='" +
                    message1 +
                    "';document.querySelector('button.absolute').disabled = false;" +
                    "document.querySelector('button.absolute').click(); })();";

            webView.evaluateJavascript(stringBuilder,null);
//                webView.evaluateJavascript(stringBuilder, new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        // After sending the message, make the WebView visible
//                        webView.setVisibility(View.VISIBLE);
//                    }
//                });
            // Clear the EditText after sending the message
            if(adcounter%2!=0) {
                if (minterstitialAd != null) {
                    minterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");

                }
   //             Toast.makeText(getBaseContext(), "Ad is Loading !!", Toast.LENGTH_SHORT).show();

            }   // TODO Auto-generated method stub

            messageEditText.setText(messageEditText.getText());
            // Simulate pressing the "A" key
            KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
            KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE);
            webView.requestFocus();
// Dispatch the key events to the currently focused view
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.dispatchKeyEvent(keyEventDown);
                currentFocus.dispatchKeyEvent(keyEventUp);
            }

            webView.evaluateJavascript(stringBuilder,null);
//messageEditText.setText("");
        }
    }

}