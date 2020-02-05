package de.stm.oses.index.database;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;


public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static ArrayList<Integer> stringToIntArray(String value) {
        try {
            if (value == null) {
                return new ArrayList<>();
            } else {
                JSONArray array = new JSONArray(value);
                ArrayList<Integer> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getInt(i));
                }
                return list;
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    @TypeConverter
    public static String intArrayToString (ArrayList<Integer> array) {
        return array == null ? null : new JSONArray(array).toString();
    }
}

