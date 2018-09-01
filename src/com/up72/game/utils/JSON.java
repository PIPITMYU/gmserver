package com.up72.game.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;

/**
 * Created by admin on 2017/6/29.
 */
public class JSON {


    public static String getJson(Object object){
        try{
            if (object == null) {
                return "";
            }
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
            mapper.writeValue(gen, object);
            gen.close();
            return sw.toString();
        }catch (Exception e){
            return null;
        }

    }
}
