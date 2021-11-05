package com.example.connectmap;

import com.example.connectmap.database.Post;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;


public class helperMethodsTest {
    @Test
    public void postInTimeTest() {
        Date postDate = new Date(1072945800000L); //2004-01-01 09:30:00 --licenses
        Post testPost = new Post("0", "test", "", 0.0, 0.0, postDate.getTime());

        int timeInterval = 24;

        Date date1 = new Date(1072911600000L); //2004-01-01 00:00:00
        Date date2 = new Date(1073084400000L); //2004-01-03 00:00:00
        Date date3 = new Date(1072738800000L); //2003-12-30 00:00:00


        assertEquals(true, HelperMethods.isPostInTime(testPost, date1, timeInterval));
        assertEquals(false, HelperMethods.isPostInTime(testPost, date2, timeInterval));
        assertEquals(false, HelperMethods.isPostInTime(testPost, date3, timeInterval));

    }
}