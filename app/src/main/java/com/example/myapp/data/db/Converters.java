package com.example.myapp.data.db;

import androidx.room.TypeConverter;
import android.net.Uri;

import com.example.myapp.data.CuisineType;

public class Converters {
    @TypeConverter public static String fromUri(Uri uri) { return uri == null ? null : uri.toString(); }
    @TypeConverter public static Uri toUri(String s) { return s == null ? null : Uri.parse(s); }

    @TypeConverter public static String fromCuisine(CuisineType c) { return c == null ? null : c.name(); }
    @TypeConverter public static CuisineType toCuisine(String s) { return s == null ? null : CuisineType.valueOf(s); }
}
