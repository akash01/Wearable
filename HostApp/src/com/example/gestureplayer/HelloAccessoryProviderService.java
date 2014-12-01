package com.example.gestureplayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.security.cert.X509Certificate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAAuthenticationToken;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class HelloAccessoryProviderService extends SAAgent {
	public static final String TAG = "HelloAccessoryProviderService";
	
	// audio processing thread
	Thread td;
	// sampling rate
	int spr = 44100;
	// audio on and off
	boolean isRunning = true;
	int amp = 1000;
	int buffersize;
	private AudioTrack audioTrack;
	
	double midOctave[] = {440,446.16,493.88,523.25,554.37,587.33,0,622.25,659.25,698.46,739.99,783.99,830.61};
	
	double ph = 0.0;
	double twopi = 8.*Math.atan(1.);
	
	public Boolean isAuthentication = false;
	public Context mContext = null;

	public static final int SERVICE_CONNECTION_RESULT_OK = 0;

	public static final int HELLOACCESSORY_CHANNEL_ID = 104;

	HashMap<Integer, HelloAccessoryProviderConnection> mConnectionsMap = null;

	private final IBinder mBinder = new LocalBinder();
	private int authCount = 1;

	private int acclvalue;

	public class LocalBinder extends Binder {
		public HelloAccessoryProviderService getService() {
			return HelloAccessoryProviderService.this;
		}
	}

	public HelloAccessoryProviderService() {
		super(TAG, HelloAccessoryProviderConnection.class);
	}

	public class HelloAccessoryProviderConnection extends SASocket {
		private int mConnectionId;

		public HelloAccessoryProviderConnection() {
			super(HelloAccessoryProviderConnection.class.getName());
		}

		@Override
		public void onError(int channelId, String errorString, int error) {
		}

		@Override
		public void onReceive(int channelId, byte[] data) {

			Time time = new Time();

			time.set(System.currentTimeMillis());

			String timeStr = " " + String.valueOf(time.minute) + ":"
					+ String.valueOf(time.second);

			String strToUpdateUI = new String(data);

			String[] parts = strToUpdateUI.split(",");
			
		
			final int Updown = Integer.parseInt(parts[1]);
			int lefright = Integer.parseInt(parts[0]);
		
			Toast.makeText(getBaseContext(),
	                new String(strToUpdateUI), Toast.LENGTH_LONG)
	                .show();	
			
			
			//[Hello X, -0.14330385384933642 Y,  1 Z,  11 Rot X ,  -6.424459934234619 Rot Y ,  -1.6328200101852417 Rot Z ,  -2.395819902420044]
			
			final List initialValue = new ArrayList();
			
			
			int[] myList; 
					
		   // System.out.println(value);
			// start a new thread to make audio
			td = new Thread() {
				
				public void run() {

					//audioTrack.stop();
					// process priority
					setPriority(Thread.MAX_PRIORITY);
					short samples[] = new short[buffersize];
					// audio loop
					while(isRunning) {
						//fr =  440 + 440*value;
						
						for(int i=0; i<buffersize;i++) {
							samples[i] = (short) (amp*Math.sin(ph));
							ph += twopi*midOctave[Updown]/spr;
						}
						audioTrack.write(samples,0,buffersize);
					}
					
					//System.out.println(accelationSquareRoot);
					//initialValue.set(0,Updown);
					audioTrack.stop();
					audioTrack.release();
				}
			};
			td.start();
			
			
			//MainActivity activity = new MainActivity();
			//activity.PlayNote(Updown,lefright);
			
			final String message = strToUpdateUI.concat(timeStr);

			final HelloAccessoryProviderConnection uHandler = mConnectionsMap.get(Integer
					.parseInt(String.valueOf(mConnectionId)));
			if(uHandler == null){
				return;
			}
			new Thread(new Runnable() {
				public void run() {
					try {
						uHandler.send(HELLOACCESSORY_CHANNEL_ID, message.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		@Override
		protected void onServiceConnectionLost(int errorCode) {

			if (mConnectionsMap != null) {
				mConnectionsMap.remove(mConnectionId);
			}
		}
	}
	
	public Integer getAcclValue(){
	  return acclvalue;
	}
	
	public void setName(int acclvalue){
	   this.acclvalue = acclvalue;
	}

    @Override
    public void onCreate() {
        super.onCreate();
        
        SA mAccessory = new SA();
        
        // buffer size, holds the size of audio block to be output
        buffersize = AudioTrack.getMinBufferSize(spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
        // create audiotrack object
     	audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,spr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT
     											,buffersize,AudioTrack.MODE_STREAM);
     		
     	audioTrack.play();
     		
        try {
        	mAccessory.initialize(this);
        } catch (SsdkUnsupportedException e) {
        	// Error Handling
        } catch (Exception e1) {
            e1.printStackTrace();
			/*
			 * Your application can not use Accessory package of Samsung
			 * Mobile SDK. You application should work smoothly without using
			 * this SDK, or you may want to notify user and close your app
			 * gracefully (release resources, stop Service threads, close UI
			 * thread, etc.)
			 */
            stopSelf();
        }

    }	    

    @Override 
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
    	/*
    	* The authenticatePeerAgent(peerAgent) API may not be working properly 
    	* depending on the firmware version of accessory device. 
        * Recommend to upgrade accessory device firmware if possible.
        */ 
    	
//    	if(authCount%2 == 1)
//    		isAuthentication = false;
//    	else
//    		isAuthentication = true; 
//    	authCount++;
    	
    	isAuthentication = false;
    	
    	if(isAuthentication) {
            Toast.makeText(getBaseContext(), "Authentication On!", Toast.LENGTH_SHORT).show();
            authenticatePeerAgent(peerAgent);
        }
    	else {
            Toast.makeText(getBaseContext(), "Authentication Off!", Toast.LENGTH_SHORT).show();
            acceptServiceConnectionRequest(peerAgent);
        }    		
    } 
    
    protected void onAuthenticationResponse(SAPeerAgent uPeerAgent,
    		SAAuthenticationToken authToken, int error) {
		
		if (authToken.getAuthenticationType() == SAAuthenticationToken.AUTHENTICATION_TYPE_CERTIFICATE_X509) {
			mContext = getApplicationContext();
			byte[] myAppKey = getApplicationCertificate(mContext);
		
			if (authToken.getKey() != null) {
				boolean matched = true;
				if(authToken.getKey().length != myAppKey.length){
					matched = false;
				}else{
					for(int i=0; i<authToken.getKey().length; i++){
						if(authToken.getKey()[i]!=myAppKey[i]){
							matched = false;
						}
					}
				}				
				if (matched) {
					acceptServiceConnectionRequest(uPeerAgent);
				}				
			}
		} else if (authToken.getAuthenticationType() == SAAuthenticationToken.AUTHENTICATION_TYPE_NONE) 
			Log.e(TAG, "onAuthenticationResponse : CERT_TYPE(NONE)");		
	}
	
	private static byte[] getApplicationCertificate(Context context) {
		if(context == null) {
			return null;
		}
		Signature[] sigs;
		byte[] certificat = null;
		String packageName = context.getPackageName();
		if (context != null) {
			try {
				PackageInfo pkgInfo = null;
				pkgInfo = context.getPackageManager().getPackageInfo(
						packageName, PackageManager.GET_SIGNATURES);
				if (pkgInfo == null) {
					return null;
				}
				sigs = pkgInfo.signatures;
				if (sigs == null) {
				} else {
					CertificateFactory cf = CertificateFactory
							.getInstance("X.509");
					ByteArrayInputStream stream = new ByteArrayInputStream(
							sigs[0].toByteArray());
					X509Certificate cert;
					cert = X509Certificate.getInstance(stream);
					certificat = cert.getPublicKey().getEncoded();
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (javax.security.cert.CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return certificat;
	}
    
	@Override
	protected void onFindPeerAgentResponse(SAPeerAgent arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket thisConnection,
			int result) {
		if (result == CONNECTION_SUCCESS) {
			
			if (thisConnection != null) {
				HelloAccessoryProviderConnection myConnection = (HelloAccessoryProviderConnection) thisConnection;

				if (mConnectionsMap == null) {
					mConnectionsMap = new HashMap<Integer, HelloAccessoryProviderConnection>();
				}

				myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);

				mConnectionsMap.put(myConnection.mConnectionId, myConnection);
			} 
		}
		else if (result == CONNECTION_ALREADY_EXIST) {
			Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}