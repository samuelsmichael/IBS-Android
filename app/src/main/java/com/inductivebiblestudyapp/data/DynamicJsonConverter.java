package com.inductivebiblestudyapp.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Converter to convert the possible string input to json.
 * @author Jason Jenkins
 * @version 0.1.0-20150617
 */
class DynamicJsonConverter implements Converter {
	
	private Gson mGson = null;
	public DynamicJsonConverter(Gson gson) {
		this.mGson = gson;
	}

    @Override 
    public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
        try {
        	//parse input as string, as it can be either.
            InputStream input = typedInput.in();
            String string = streamToString(input);
            input.close(); 

            if (String.class.equals(type)) {
            	//it was already a string
                return string;
            } else {
            	//convert json string to json
                return mGson.fromJson(string, type); 
            }
        } catch (Exception e) { 
        	// wrap with ConversionException for retrofit to process
            throw new ConversionException(e); 
        }
    }

    @Override 
    public TypedOutput toBody(Object object) {
    	// nothing to do here.
        return null;
    }

    private static String streamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder output = new StringBuilder();
        
        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append("\r\n");
        }
        
        return output.toString();
    }
}
