package com.hackathon.cmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hackathon.cmc.adapter.PathJSONParser;
import com.hackathon.cmc.locationplacesautocomplete.CustomAutoCompleteTextView;
import com.hackathon.cmc.locationplacesautocomplete.PlaceJSONParser;
import com.hackathon.cmc.adapter.HttpConnection;


public class HomeFragment extends Fragment {
	
	AutoCompleteTextView source, destination;
	Button search;
	PlacesTask placesTask;
	ParserTask parserTask;
	// Google Map
    private GoogleMap googleMap;
    private SupportMapFragment fragment;
    MarkerOptions marker;
    public Double Lat = null;
    public Double Lng = null;
    boolean firstTime = false;
    LatLng latLng;
    MarkerOptions markerOptions;
    InputMethodManager imm;
    private Circle mCircle;
    private Marker mMarker;
    ArrayList<LatLng> myLatLng = new ArrayList<LatLng>();
    private static double RADIUS = 2000;

	private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
			-73.998585);
	private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
	private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);


	public HomeFragment(){}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    FragmentManager fm = getChildFragmentManager();
	    fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
	    if (fragment == null) {
	        fragment = SupportMapFragment.newInstance();
	        fm.beginTransaction().replace(R.id.map, fragment).commit();
	    }
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		firstTime = false;
	}
	@Override
	    public void onResume() {
	        super.onResume();
	        if (googleMap == null) {
	        	googleMap = fragment.getMap();
	        	
	        	googleMap.setMyLocationEnabled(true);
	        	googleMap.getUiSettings().setMyLocationButtonEnabled(true);
	        	googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
					
					@Override
					public boolean onMyLocationButtonClick() {
						// TODO Auto-generated method stub
						firstTime = false;
						imm.hideSoftInputFromWindow(source.getWindowToken(), 0);
						imm.hideSoftInputFromWindow(destination.getWindowToken(), 0);
						focusToCurrentLocation();
						return true;
					}
				});
	        	// Check if we were successful in obtaining the map.
//	            if (googleMap != null) {
//	            	firstTime = false;
//	            	focusToCurrentLocation();
//
//	            }
	            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick(Marker arg0) {
						// TODO Auto-generated method stub
						Log.v("on marker click", arg0.getTitle());
					}
				});
	       
	        }
	    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        source = (AutoCompleteTextView) rootView.findViewById(R.id.source);
		destination = (AutoCompleteTextView) rootView.findViewById(R.id.destination);

		autocomplete(source);
		autocomplete(destination);
		search = (Button) rootView.findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					googleMap = fragment.getMap();

					MarkerOptions options = new MarkerOptions();
					options.position(LOWER_MANHATTAN);
					options.position(BROOKLYN_BRIDGE);
					options.position(WALL_STREET);
					googleMap.addMarker(options);
					String url = getMapsApiDirectionsUrl();
					ReadTask downloadTask = new ReadTask();
					downloadTask.execute(url);

					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BROOKLYN_BRIDGE,
							13));
					addMarkers();

			}
		});
        return rootView;
    }


	public void autocomplete(final AutoCompleteTextView object){
		object.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0);
		object.setThreshold(1);
		imm = (InputMethodManager)getActivity().getBaseContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		object.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// TODO Auto-generated method stub
				imm.hideSoftInputFromWindow(object.getWindowToken(), 0);
				HashMap<String, String> hm = (HashMap<String, String>)parent.getItemAtPosition(position);
				Log.v("selected",hm.get("description"));
				String selectedLoc = hm.get("description");
				if(selectedLoc!=null && !selectedLoc.equals("")){
					new GeocoderTask().execute(selectedLoc);
				}
			}
		});
		String value = "";//any text you are pre-filling in the EditText
		final Drawable x = getResources().getDrawable(android.R.drawable.presence_offline);//your x image, this one from standard android images looks pretty good actually
		x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
		object.setCompoundDrawables(null, null, value.equals("") ? null : x, null);
		object.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (object.getCompoundDrawables()[2] == null) {
					return false;
				}
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}
				if (event.getX() > object.getWidth() - object.getPaddingRight() - x.getIntrinsicWidth()) {
					object.setText("");
					object.setCompoundDrawables(null, null, null, null);
				}
				return false;
			}
		});

		object.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				object.setCompoundDrawables(null, null, object.getText().toString().equals("") ? null : x, null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				// TODO Auto-generated method stub
				placesTask = new PlacesTask();
				placesTask.execute(s.toString());

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void updateMarkerWithCircle(LatLng position) {
	    mCircle.setCenter(position);
	    mMarker.setPosition(position);
	}

	private void drawCircle(LatLng position){
	    int strokeColor = Color.argb(255, 255, 0, 0); //red outline
	    int shadeColor = Color.argb(60, 255, 0, 0); //transparent red fill
	    CircleOptions circleOptions = new CircleOptions().center(position).radius(RADIUS).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2);
	    mCircle = googleMap.addCircle(circleOptions);

	    //MarkerOptions markerOptions = new MarkerOptions().position(position);
	    //mMarker = googleMap.addMarker(markerOptions);
	}
	
	private void addNearByMarkers(LatLng oldPosition){
		float[] results = new float[1];
		for(LatLng newPosition: myLatLng){
			Location.distanceBetween(oldPosition.latitude, oldPosition.longitude,
		            newPosition.latitude, newPosition.longitude, results);
			Log.v("result", ""+results[0]);
			Log.v("old lati", ""+oldPosition.latitude);
			Log.v("old longi", ""+oldPosition.longitude);
			Log.v("new lati", ""+newPosition.latitude);
			Log.v("new longi", ""+newPosition.longitude);
			if(results[0] < RADIUS){
				String address = getAddressFromLatLng(newPosition.latitude, newPosition.longitude);
				marker = new MarkerOptions().position(new LatLng(newPosition.latitude,newPosition.longitude)).title(address);
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				googleMap.addMarker(marker);
			}
		}
	}
	
	private String getAddressFromLatLng(double Lat, double Lng){
		Geocoder geocoder;
 	   	List<Address> addresses;
 	   	String address = "";
 	   	geocoder = new Geocoder(getActivity(), Locale.getDefault());
 	   	try {
 	   			addresses = geocoder.getFromLocation(Lat, Lng, 1);
				address = addresses.get(0).getAddressLine(1);
				
 	   		} 
 	   	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 	   	}
 	   	return address;
	}
	
	/** A method to get current location and animate and set marker*/
	private void focusToCurrentLocation(){
		googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

	           @Override
	           public void onMyLocationChange(Location arg0) {
	            // TODO Auto-generated method stub
	        	   if(!firstTime){
	        		   firstTime = true;
		        	   Lat = arg0.getLatitude();
		        	   Lng = arg0.getLongitude();
		        	   
		        	   String address = getAddressFromLatLng(Lat,Lng);
		        	   source.setText(address);
		        	   // Clears all the existing markers on the map
		               googleMap.clear();
		        	   
		        	   marker = new MarkerOptions().position(new LatLng(Lat,Lng)).title("My Current Location: " + address);
		        	   googleMap.addMarker(marker);
		        	   CameraPosition cameraPosition = new CameraPosition.Builder().target(
			                    new LatLng(Lat,Lng)).zoom(12).build();
			           googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		        	   
			           //draw Circle and Marker
			           if(mCircle == null || mMarker == null){
			        	   drawCircle(new LatLng(Lat,Lng));
			            }else{
			                updateMarkerWithCircle(new LatLng(Lat,Lng));
			            }
			           
			           //add markers around
			        	addNearByMarkers(new LatLng(Lat,Lng));
	        	   }

	        	   
	        	   
	           }

     	});
	}
	
	
	// An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity().getBaseContext());
            List<Address> addresses = null;
 
            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }
 
        @Override
        protected void onPostExecute(List<Address> addresses) {
 
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getActivity().getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
 
            // Clears all the existing markers on the map
            googleMap.clear();
 
            Address address = (Address) addresses.get(0);
            
            // Creating an instance of GeoPoint, to display in Google Map
            latLng = new LatLng(address.getLatitude(), address.getLongitude());

            String addressText = String.format("%s, %s",
            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
            address.getCountryName());

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(addressText);

            googleMap.addMarker(markerOptions);

            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);

        	googleMap.moveCamera(center);
        	googleMap.animateCamera(zoom);
            
        	//draw Circle
        	if(mCircle == null || mMarker == null){
        		drawCircle(latLng);
            }else{
                updateMarkerWithCircle(latLng);
            }
            
        	//add markers around
        	addNearByMarkers(latLng);
            
        }
    }
	
	/** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
                URL url = new URL(strUrl);                

                // Creating an http connection to communicate with url 
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url 
                urlConnection.connect();

                // Reading data from url 
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                        sb.append(line);
                }
                
                data = sb.toString();

                br.close();

        }catch(Exception e){
                Log.d("error downloading url", e.toString());
        }finally{
                iStream.close();
                urlConnection.disconnect();
        }
        return data;
     }	
	
	// Fetches all places from GooglePlaces AutoComplete Web Service
	private class PlacesTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... place) {
			// For storing data from web service
			String data = "";
			
			// Obtain browser key from https://code.google.com/apis/console
			String key = "key=AIzaSyDAgGiQT-etccxauMLRMyuRDKWum_bkJn4";
			
			String input="";
			
			try {
				input = "input=" + URLEncoder.encode(place[0], "utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}		
			
			
			// place type to be searched
			String types = "types=geocode";
			
			// Sensor enabled
			String sensor = "sensor=false";			
			
			// Building the parameters to the web service
			String parameters = input+"&"+types+"&"+sensor+"&"+key;
			
			// Output format
			String output = "json";
			
			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
	
			try{
				// Fetching the data from web service in background
				data = downloadUrl(url);
			}catch(Exception e){
                Log.d("Background Task",e.toString());
			}
			return data;		
		}
		
		@Override
		protected void onPostExecute(String result) {			
			super.onPostExecute(result);
			
			// Creating ParserTask
			parserTask = new ParserTask();
			
			// Starting Parsing the JSON string returned by Web Service
			parserTask.execute(result);
		}		
	}
	
	
	/** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

    	JSONObject jObject;
    	
		@Override
		protected List<HashMap<String, String>> doInBackground(String... jsonData) {			
			
			List<HashMap<String, String>> places = null;
			
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();
            
            try{
            	jObject = new JSONObject(jsonData[0]);
            	
            	// Getting the parsed data as a List construct
            	places = placeJsonParser.parse(jObject);

            }catch(Exception e){
            	Log.d("Exception", e.toString());
            }
            return places;
		}
		
		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {			
			
				String[] from = new String[] { "description"};
				int[] to = new int[] { android.R.id.text1 };
				
				// Creating a SimpleAdapter for the AutoCompleteTextView			
				SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), result, R.layout.autocomplete_dropdown, from, to);				
				
				// Setting the adapter
				if(source.isFocused())
					source.setAdapter(adapter);
				else if(destination.isFocused())
					destination.setAdapter(adapter);
		}
    }

	private String getMapsApiDirectionsUrl() {
		String waypoints = "waypoints=optimize:true|"
				+ LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
				+ "|" + "|" + BROOKLYN_BRIDGE.latitude + ","
				+ BROOKLYN_BRIDGE.longitude + "|" + WALL_STREET.latitude + ","
				+ WALL_STREET.longitude;

		String sensor = "sensor=false";
		String origin = "origin=" + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude;
		String destination = "destination=" + WALL_STREET.latitude + "," + WALL_STREET.longitude;
		String params = origin + "&" + destination + "&%20" + waypoints + "&" + sensor;
		String output = "json";

		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + params;
		return url;
	}

	private void addMarkers() {
		if (googleMap != null) {
			googleMap.addMarker(new MarkerOptions().position(BROOKLYN_BRIDGE)
					.title("First Point"));
			googleMap.addMarker(new MarkerOptions().position(LOWER_MANHATTAN)
					.title("Second Point"));
			googleMap.addMarker(new MarkerOptions().position(WALL_STREET)
					.title("Third Point"));
		}
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new DrawPolyTask().execute(result);
		}
	}

	private class DrawPolyTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;

			// traversing through routes
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);

				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(5);
				polyLineOptions.color(Color.BLUE);
			}
			googleMap.addPolyline(polyLineOptions);
		}
	}
}
